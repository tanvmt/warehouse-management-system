package client.service;

import com.group9.warehouse.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientService {
    private static GrpcClientService instance;
    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;
    private UserManagementServiceGrpc.UserManagementServiceBlockingStub userStub;
    private ProductManagementServiceGrpc.ProductManagementServiceBlockingStub productStub;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

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
                        .usePlaintext()
                        .build();
                authStub = AuthServiceGrpc.newBlockingStub(channel);
                userStub = UserManagementServiceGrpc.newBlockingStub(channel);
                productStub = ProductManagementServiceGrpc.newBlockingStub(channel);
                warehouseStub = WarehouseServiceGrpc.newBlockingStub(channel);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public AuthServiceGrpc.AuthServiceBlockingStub getAuthStub() {
        return authStub;
    }
    
    public UserManagementServiceGrpc.UserManagementServiceBlockingStub getUserStub() {
        return userStub;
    }
    
    public ProductManagementServiceGrpc.ProductManagementServiceBlockingStub getProductStub() {
        return productStub;
    }
    
    public WarehouseServiceGrpc.WarehouseServiceBlockingStub getWarehouseStub() {
        return warehouseStub;
    }

    public void close() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}