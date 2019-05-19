package edu.yuferov.chat.server.io;

import edu.yuferov.chat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import static edu.yuferov.chat.common.Constants.EOF;
import static edu.yuferov.chat.common.Constants.BUFFER_SIZE;
import static edu.yuferov.chat.common.Constants.SELECTOR_WAIT_TIME;

class ServerWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ServerWorker.class);

    private final Selector selector;
    private final Server server;
    private final Duration maxInactivityDuration;
    private final DataListener dataListener;

    class ConnectionContext {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        Instant lastActivity = Instant.now();

        boolean isInactive(Duration maxInactivityDuration) {
            Duration inactiveDuration = Duration.between(lastActivity, Instant.now());
            return inactiveDuration.compareTo(maxInactivityDuration) > 0;
        }

        void resetActivity() {
            lastActivity = Instant.now();
        }
    }

    ServerWorker(Server server, Duration maxInactivityDuration, DataListener dataListener) throws IOException {
        this.maxInactivityDuration = maxInactivityDuration;
        this.dataListener = dataListener;
        this.selector = Selector.open();
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && selector.isOpen()) {
                kickForInactivity();
                if (selector.select(SELECTOR_WAIT_TIME) == 0) {
                    continue;
                }
                handleSelectedKeys(selector.selectedKeys().iterator());
            }
        } catch (IOException e) {
            log.error("Error occurred while selector wait for events", e);
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("Error occurred while close selector", e);
            }
        }
    }

    void close() throws IOException {
        selector.close();
    }

    void registerChannel(SelectableChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, new ConnectionContext());
    }

    void registerServerSocket(SelectableChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void handleSelectedKeys(Iterator<SelectionKey> iterator) {
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            handleKey(key);
        }
    }

    private void handleKey(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                acceptConnection(key);
            } else if (key.isReadable()) {
                readData(key);
            } else {
                throw new RuntimeException("unexpected event");
            }
        } catch (Throwable e) {
            log.error("Error occurred while handle socket event", e);
            cancelKeyAndReleaseSocket(key);
        }
    }

    private void cancelKeyAndReleaseSocket(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            channel.socket().close();
            key.channel().close();
        } catch (IOException e) {
            log.error("Error occurred while close socket", e);
        }
        key.cancel();
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        server.acceptConnection();
    }

    private void readData(SelectionKey key) throws Exception {
        ConnectionContext context = (ConnectionContext) key.attachment();
        context.resetActivity();
        ByteBuffer buffer = context.buffer;
        SocketChannel channel = (SocketChannel) key.channel();
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            server.clientDisconnected(channel);
            cancelKeyAndReleaseSocket(key);
            return;
        }
        log.debug("Data chunk received, buffer state {}", buffer.toString());
        parseData(channel, buffer, bytesRead);
    }

    private void parseData(SocketChannel channel, ByteBuffer buffer, int bytesRead) throws Exception {
        int end = Utils.findEndOfRequest(buffer, bytesRead);
        if (end == -1) {
            return;
        }
        byte[] payload = dataListener.parseData(Arrays.copyOf(buffer.array(), end));
        byte[] response = Arrays.copyOf(payload, payload.length + 1);
        response[payload.length] = EOF;
        channel.write(ByteBuffer.wrap(response));
        buffer.clear();
    }

    private void kickForInactivity() {
        selector.keys().stream().filter(key -> key.attachment() != null).forEach(key -> {
            ConnectionContext context = (ConnectionContext) key.attachment();
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isOpen() && context.isInactive(maxInactivityDuration)) {
                try {
                    log.info("User {} kicked due to inactivity", channel.getRemoteAddress());
                } catch (IOException ignore) {
                }
                cancelKeyAndReleaseSocket(key);
            }
        });
    }
}
