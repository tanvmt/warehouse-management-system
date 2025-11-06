package client.service;

import com.group9.warehouse.grpc.LoginRequest;
import com.group9.warehouse.grpc.LoginResponse;

public class AuthService {
    private String errorMessage;
    private GrpcClientService grpcService; 

    public AuthService() {
        this.grpcService = GrpcClientService.getInstance();
    }

    public boolean login(String ip, int port, String username, String password) {
        boolean connected = grpcService.connect(ip, port); 
        if (!connected) {
            this.errorMessage = "Lỗi: Không thể kết nối tới Server.";
            return false;
        }

        try {
            LoginRequest request = LoginRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build();
            
            LoginResponse response = grpcService.getStub().login(request);

            if (response.getSuccess()) {
                SessionManager.createSession(username, response.getRole());
                return true;
            } else {
                this.errorMessage = "Đăng nhập thất bại: " + response.getMessage();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.errorMessage = "Lỗi gRPC: " + e.getMessage();
            return false;
        }
    }
    
    public String getErrorMessage() { return errorMessage; }
}