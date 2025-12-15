package client.service;

import com.group9.warehouse.grpc.LoginRequest;
import com.group9.warehouse.grpc.LoginResponse;
import com.group9.warehouse.grpc.AuthServiceGrpc;
import com.group9.warehouse.grpc.EmptyRequest;
import com.group9.warehouse.grpc.ProfileResponse;

import io.grpc.Status; 
import io.grpc.StatusRuntimeException;

public class AuthService {
    private String errorMessage;
    private GrpcClientService grpcService;
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;

    public AuthService() {
        this.grpcService = GrpcClientService.getInstance();
        // this.authStub = grpcService.getAuthStub();
    }

    public boolean login(String ip, int port, String username, String password) {
        boolean connected = grpcService.connect(ip, port); 
        if (!connected) {
            this.errorMessage = "Lỗi: Không thể kết nối tới Server.";
            return false;
        }

        try {
            AuthServiceGrpc.AuthServiceBlockingStub authStub = grpcService.getAuthStub();
            LoginRequest request = LoginRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build();
            
            LoginResponse response = authStub.login(request);

            if (response.getSuccess()) {
                String token = response.getToken();
                SessionManager.createSession(null, null, token, null);
                
                ProfileResponse profileResponse = authStub.getUserProfile(EmptyRequest.newBuilder().build());
                String fullName = profileResponse.getProfile().getFullName();
                
                SessionManager.createSession(username, response.getRole(), token, fullName);
                return true;
            } else {
                this.errorMessage = "Đăng nhập thất bại: " + response.getMessage();
                return false;
            }
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            
            Status.Code code = e.getStatus().getCode();
            if (code == Status.Code.INVALID_ARGUMENT || code == Status.Code.UNAUTHENTICATED) {
                this.errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng.";
            } else if (code == Status.Code.UNAVAILABLE) {
                this.errorMessage = "Không thể kết nối đến máy chủ. Vui lòng thử lại.";
            } else {
                this.errorMessage = "Đã xảy ra lỗi. Vui lòng thử lại sau.";
                System.err.println("Lỗi gRPC không xác định: " + e.getStatus());
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            this.errorMessage = "Đã xảy ra lỗi không xác định: " + e.getMessage();
            return false;
        }
    }
    
    public String getErrorMessage() { return errorMessage; }
}