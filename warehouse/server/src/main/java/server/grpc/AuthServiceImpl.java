package server.grpc;

import com.group9.warehouse.grpc.*;
import io.grpc.stub.StreamObserver;
import server.interceptor.AuthInterceptor; // Để lấy USERNAME_CONTEXT_KEY
import server.model.User;
import server.service.AuthService;
import server.service.JwtService;
import server.validator.AuthRequestValidator;


public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;
    private final JwtService jwtService;
    public AuthServiceImpl(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        AuthRequestValidator.validateLogin(request);

        User user = authService.validateUser(request.getUsername(), request.getPassword());
        LoginResponse.Builder responseBuilder = LoginResponse.newBuilder();
        String token = jwtService.generateToken(user);

        responseBuilder
                .setSuccess(true)
                .setMessage("Đăng nhập thành công!")
                .setRole(user.getRole())
                .setToken(token);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserProfile(EmptyRequest request, StreamObserver<ProfileResponse> responseObserver) {

        String username = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        UserProfile userProfile = authService.getUserProfile(username);
        ProfileResponse.Builder response = ProfileResponse.newBuilder();

        response.setSuccess(true)
                .setMessage("Get UserProfile success!!!")
                .setProfile(userProfile);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateUserProfile(UpdateProfileRequest request, StreamObserver<ServiceResponse> responseObserver) {
        AuthRequestValidator.validateUpdateUser(request);

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
        AuthRequestValidator.validateChangePassword(request);

        String username = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        authService.changePassword(
                username, request.getOldPassword(), request.getNewPassword()
        );

        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Đổi mật khẩu thành công")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}