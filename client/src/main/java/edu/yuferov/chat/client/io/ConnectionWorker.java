package edu.yuferov.chat.client.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yuferov.chat.common.Utils;
import edu.yuferov.chat.common.dto.requests.Request;
import edu.yuferov.chat.common.dto.responses.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static edu.yuferov.chat.common.Constants.BUFFER_SIZE;
import static edu.yuferov.chat.common.Constants.EOF;

class ConnectionWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ConnectionWorker.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BlockingQueue<Request> requests = new ArrayBlockingQueue<>(10);
    private final Connection connection;
    private final SocketAddress address;
    private SocketChannel channel;

    ConnectionWorker(Connection connection, String address, int port) {
        this.connection = connection;
        this.address = new InetSocketAddress(address, port);
    }

    @Override
    public void run() {
        try (SocketChannel ch = SocketChannel.open()){
            channel = ch;
            connect();
            processRequests();
        } catch (IOException e) {
            log.error("error while creating channel object", e);
        } finally {
            channel = null;
        }
        connection.disconnected();
        log.info("execution finished");
    }

    private void processRequests() {
        try {
            while (true) {
                Request request = requests.take();
                byte[] requestData = serializeRequest(request);
                sendRequest(requestData);
                byte[] responseData = waitResponse();
                Response response = deserializeResponse(responseData);
                connection.handleResponse(response);
            }
        } catch (InterruptedException e) {
            log.info("interrupted", e);
        } catch (Exception e) {
            log.error("error occurred", e);
        }
    }

    private void connect() throws IOException {
        try {
            channel.connect(this.address);
            connection.connected();
        } catch (IOException e) {
            connection.connectionError(e.getMessage());
            throw e;
        }
    }

    void addRequest(Request request) {
        requests.add(request);
    }

    private byte[] serializeRequest(Request request) throws IOException {
        return objectMapper.writeValueAsString(request).getBytes();
    }

    private void sendRequest(byte[] request) throws IOException {
        byte[] data = Arrays.copyOf(request, request.length + 1);
        data[request.length] = EOF;
        channel.write(ByteBuffer.wrap(data));
    }

    private byte[] waitResponse() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead;
        int end;
        do {
            bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                throw new RuntimeException("server has been disconnected");
            }
            end = Utils.findEndOfRequest(buffer, bytesRead);
        } while (end == -1);
        return Arrays.copyOf(buffer.array(), end);
    }

    private Response deserializeResponse(byte[] response) throws IOException {
        return objectMapper.readValue(response, Response.class);
    }
}
