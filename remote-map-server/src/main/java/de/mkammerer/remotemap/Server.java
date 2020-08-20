package de.mkammerer.remotemap;

import de.mkammerer.remotemap.grpc.RemoteMapService;
import de.mkammerer.remotemap.storage.FileStorage;
import de.mkammerer.remotemap.storage.Storage;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private static final int PORT = 12345;
    private static final Path STORAGE_FILE = Paths.get("storage.bin");
    private static final long SAVE_DELAY_SECONDS = 5;

    public static void main(String[] args) throws IOException, InterruptedException {
        Storage storage = new FileStorage(new ConcurrentHashMap<>());
        try {
            if (Files.exists(STORAGE_FILE)) {
                storage.load(STORAGE_FILE);
            }

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(() -> saveStorage(storage), SAVE_DELAY_SECONDS, SAVE_DELAY_SECONDS, TimeUnit.SECONDS);

            io.grpc.Server server = ServerBuilder.forPort(PORT)
                .directExecutor()
                .addService(new RemoteMapService(storage))
                .build();

            LOGGER.debug("Starting server on port {} ...", PORT);
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server, scheduler)));
            LOGGER.info("Server is running in port {}", PORT);
            server.awaitTermination();
        } finally {
            LOGGER.info("Application is shutting down, saving storage ...");
            saveStorage(storage);
            LOGGER.info("Done");
        }
    }

    private static void saveStorage(Storage storage) {
        try {
            storage.save(STORAGE_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to save storage!", e);
        }
    }

    private static void stop(io.grpc.Server server, ScheduledExecutorService scheduler) {
        LOGGER.debug("Shutting down server ...");
        try {
            server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            LOGGER.debug("Shut down server");
        } catch (InterruptedException e) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LOGGER.debug("Shutting down scheduler ...");
        try {
            scheduler.shutdown();
            scheduler.awaitTermination(10, TimeUnit.SECONDS);
            LOGGER.debug("Shut down scheduler");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
