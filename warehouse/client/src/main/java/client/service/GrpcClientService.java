package client.service;

import com.group9.warehouse.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.ClientInterceptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class GrpcClientService {
    private static GrpcClientService instance;
    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;
    private UserManagementServiceGrpc.UserManagementServiceBlockingStub userStub;
    private ProductManagementServiceGrpc.ProductManagementServiceBlockingStub productStub;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = 
        Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);

        private static class AuthInterceptor implements ClientInterceptor {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method,
                    CallOptions callOptions,
                    Channel next) {

                if (method.getFullMethodName().equals("AuthService/Login")) {
                    return next.newCall(method, callOptions);
                }
                
                return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        String token = SessionManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            headers.put(AUTHORIZATION_METADATA_KEY, "Bearer " + token);
                        }
                        super.start(responseListener, headers);
                    }
                };
            }
        }

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

                ClientInterceptor authInterceptor = new AuthInterceptor();

                authStub = AuthServiceGrpc.newBlockingStub(channel).withInterceptors(authInterceptor);
                userStub = UserManagementServiceGrpc.newBlockingStub(channel).withInterceptors(authInterceptor);
                productStub = ProductManagementServiceGrpc.newBlockingStub(channel).withInterceptors(authInterceptor);
                warehouseStub = WarehouseServiceGrpc.newBlockingStub(channel).withInterceptors(authInterceptor);
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