package client.service;

import com.group9.warehouse.grpc.WarehouseServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientService {
    private static GrpcClientService instance;
    private ManagedChannel channel;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub blockingStub;

    private GrpcClientService() {}

    public static synchronized GrpcClientService getInstance() {
        if (instance == null) {
            instance = new GrpcClientService();
        }
        return instance;
    }

    public boolean connect(String ip, int port) {
        try {
            if (channel == null || channel.isShutdown()) {
                channel = ManagedChannelBuilder.forAddress(ip, port)
                        .usePlaintext() // Tắt SSL/TLS để test
                        .build();
                blockingStub = WarehouseServiceGrpc.newBlockingStub(channel);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public WarehouseServiceGrpc.WarehouseServiceBlockingStub getStub() {
        return blockingStub;
    }

    public void close() {
        if (channel != null) {
            channel.shutdown();
        }
        instance = null;
    }
}