package edu.yuferov.chat.server.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Server implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    @Autowired
    private ServerSocketChannel serverSocket;

    @Autowired
    private DataListener dataListener;

    @Value("${server.workers}")
    private Integer workerCount;

    @Value("${server.max-clients}")
    private Integer maxClientsCount;

    @Value("${server.max-inactivity-sec}")
    private Integer maxInactivityDurationSec;

    private final List<ServerWorker> workers = new ArrayList<>();
    private final AtomicInteger clientsCount = new AtomicInteger();
    private int prevWorker;

    @Override
    public void run() {
        try {
            Executor executor = Executors.newFixedThreadPool(workerCount - 1);
            ServerWorker mainWorker = createWorker();
            workers.add(mainWorker);
            for (int i = 0; i < workerCount - 1; i++) {
                ServerWorker worker = createWorker();
                executor.execute(worker);
                workers.add(worker);
            }
            mainWorker.registerServerSocket(serverSocket);
            mainWorker.run();
            for (int i = 1; i < workerCount; i++) {
                workers.get(i).close();
            }
        } catch (Throwable e) {
            log.error("Error occurred while serving", e);
        }
    }

    public void cancel() throws IOException {
        workers.get(0).close();
    }

    void acceptConnection() throws IOException {
        if (clientsCount.intValue() >= maxClientsCount) {
            SocketChannel channel = serverSocket.accept();
            log.warn("User {} connection refused due to max client count reached", channel.getRemoteAddress());
            channel.socket().close();
            channel.close();
            return;
        }
        clientsCount.incrementAndGet();
        prevWorker = (prevWorker + 1) % workerCount;
        SocketChannel client = serverSocket.accept();
        log.info("User {} accepted to connect", client.getRemoteAddress());
        workers.get(prevWorker).registerChannel(client);
    }

    void clientDisconnected(SocketChannel channel) throws IOException {
        log.info("User {} disconnected", channel.getRemoteAddress());
        clientsCount.decrementAndGet();
    }

    private ServerWorker createWorker() throws IOException {
        return new ServerWorker(this, Duration.of(maxInactivityDurationSec, ChronoUnit.SECONDS), dataListener);
    }
}
