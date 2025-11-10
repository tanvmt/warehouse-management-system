package server.grpc;

import com.group9.warehouse.grpc.*;
import io.grpc.stub.StreamObserver;
import server.interceptor.AuthInterceptor; // Để lấy USERNAME_CONTEXT_KEY
import server.model.User;
import server.service.AuthService;

import java.util.Optional;

// Quan trọng: extends AuthServiceGrpc...
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    public AuthServiceImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        Optional<User> userOptional = authService.validateUser(request.getUsername(), request.getPassword());

        LoginResponse.Builder responseBuilder = LoginResponse.newBuilder();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = authService.generateJwtToken(user); // Tạo token

            responseBuilder
                    .setSuccess(true)
                    .setMessage("Đăng nhập thành công!")
                    .setRole(user.getRole())
                    .setToken(token); // Trả token về
        } else {
            responseBuilder
                    .setSuccess(false)
                    .setMessage("Sai tên đăng nhập, mật khẩu, hoặc tài khoản đã bị khóa.");
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserProfile(EmptyRequest request, StreamObserver<ProfileResponse> responseObserver) {
        // Lấy username từ Context (đã được Interceptor xác thực)
        String username = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        Optional<User> userOptional = authService.getUserProfile(username);
        ProfileResponse.Builder response = ProfileResponse.newBuilder();

        if (userOptional.isPresent()) {
            response.setSuccess(true).setProfile(convertUserToProfile(userOptional.get()));
        } else {
            response.setSuccess(false).setMessage("Không tìm thấy thông tin user.");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateUserProfile(UpdateProfileRequest request, StreamObserver<ServiceResponse> responseObserver) {
        String username = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        boolean success = authService.updateUserProfile(username, request);
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Cập nhật thành công" : "Cập nhật thất bại")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<ServiceResponse> responseObserver) {
        String username = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        boolean success = authService.changePassword(
                username, request.getOldPassword(), request.getNewPassword()
        );

        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Đổi mật khẩu thành công" : "Đổi mật khẩu thất bại (sai mật khẩu cũ?)")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Hàm helper để convert
    public static UserProfile convertUserToProfile(User user) {
        UserProfile.Builder builder = UserProfile.newBuilder();
        builder.setUsername(user.getUsername())
                .setRole(user.getRole())
                .setIsActive(user.isActive());

        // Các trường có thể null
        if (user.getFullName() != null) builder.setFullName(user.getFullName());
        if (user.getEmail() != null) builder.setEmail(user.getEmail());
        if (user.getPhone() != null) builder.setPhone(user.getPhone());
        if (user.getSex() != null) builder.setSex(user.getSex());
        if (user.getDateOfBirth() != null) builder.setDateOfBirth(user.getDateOfBirth());

        return builder.build();
    }
}