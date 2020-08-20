package de.mkammerer.remotemap.client;

import com.google.protobuf.ByteString;
import de.mkammerer.remotemap.grpc.generated.DeleteRequest;
import de.mkammerer.remotemap.grpc.generated.DeleteResponse;
import de.mkammerer.remotemap.grpc.generated.GetRequest;
import de.mkammerer.remotemap.grpc.generated.GetResponse;
import de.mkammerer.remotemap.grpc.generated.PutRequest;
import de.mkammerer.remotemap.grpc.generated.PutResponse;
import de.mkammerer.remotemap.grpc.generated.RemoteMapGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private static final int PORT = 12345;

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", PORT).usePlaintext().build();
        try {
            RemoteMapGrpc.RemoteMapBlockingStub client = RemoteMapGrpc.newBlockingStub(channel);
            LOGGER.info("Running ...");
            run(client);
            LOGGER.info("Done");
        } finally {
            try {
                channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void run(RemoteMapGrpc.RemoteMapBlockingStub client) {
        int start = 0;
        int end = 1_000_000;

        put(client, start, end);

        get(client, start, end);

        delete(client, start, end);
    }

    private static void put(RemoteMapGrpc.RemoteMapBlockingStub client, int start, int end) {
        long startTime = System.nanoTime();
        for (int i = start; i < end; i++) {
            ByteString keyAndValue = ByteString.copyFromUtf8(Integer.toString(i));

            PutResponse response = client.put(PutRequest.newBuilder().setKey(keyAndValue).setValue(keyAndValue).build());
        }
        long endTime = System.nanoTime();
        LOGGER.info("Put: {}", Duration.ofNanos(endTime - startTime));
    }

    private static void get(RemoteMapGrpc.RemoteMapBlockingStub client, int start, int end) {
        long startTime;
        long endTime;
        startTime = System.nanoTime();
        for (int i = start; i < end; i++) {
            ByteString key = ByteString.copyFromUtf8(Integer.toString(i));

            GetResponse response = client.get(GetRequest.newBuilder().setKey(key).build());
        }
        endTime = System.nanoTime();
        LOGGER.info("Get: {}", Duration.ofNanos(endTime - startTime));
    }

    private static void delete(RemoteMapGrpc.RemoteMapBlockingStub client, int start, int end) {
        long startTime;
        long endTime;
        startTime = System.nanoTime();
        for (int i = start; i < end; i++) {
            ByteString key = ByteString.copyFromUtf8(Integer.toString(i));

            DeleteResponse response = client.delete(DeleteRequest.newBuilder().setKey(key).build());
        }
        endTime = System.nanoTime();
        LOGGER.info("Delete: {}", Duration.ofNanos(endTime - startTime));
    }
}
