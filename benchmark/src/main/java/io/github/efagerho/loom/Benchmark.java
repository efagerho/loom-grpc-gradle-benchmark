package io.github.efagerho.loom;

import io.github.efagerho.loom.grpc.Echo;
import io.github.efagerho.loom.grpc.EchoServiceGrpc;
import io.github.efagerho.loom.grpc.EchoServiceGrpc.EchoServiceBlockingStub;
import io.github.efagerho.loom.grpc.HelloRequest;
import io.github.efagerho.loom.grpc.HelloResponse;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Benchmark {

    private static AtomicLong requests = new AtomicLong(0);
    private static final int NUM_CLIENTS = getEnvInteger("NUM_CLIENTS");
    private static final HelloRequest request = HelloRequest.newBuilder().setName("test").build();

    private static void benchmark(EchoServiceBlockingStub blockingStub) {
        while (true) {
            try {
                blockingStub.hello(request);
                requests.incrementAndGet();
            } catch (Exception e) {
                System.out.printf("RPC failed: %s\n", e.getCause());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = Grpc.newChannelBuilder("localhost:8080", InsecureChannelCredentials.create()).build();
        EchoServiceBlockingStub blockingStub = EchoServiceGrpc.newBlockingStub(channel);

        System.out.println("Running benchmark for 60 seconds");

        Thread stats = new Thread("Stats") {
            public void run() {
                while (true) {
                    try {
                        System.out.format("%d RPS\n", requests.get());
                        requests.set(0);
                        Thread.sleep(1000);
                    } catch (Exception e) {}
                }
            }
        };
        stats.start();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            Thread.ofVirtual().start(() -> benchmark(blockingStub));
        }

        Thread.sleep(60000);
        System.exit(0);
    }

    private static int getEnvInteger(String key) {
        String value = System.getenv(key);
        return value == null ? 1 : Integer.parseInt(value);
    }
}
