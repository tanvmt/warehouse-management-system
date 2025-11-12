package server.grpc;

import com.group9.warehouse.grpc.*;
import io.grpc.stub.StreamObserver;
import server.service.UserService;

public class UserManagementServiceImpl extends UserManagementServiceGrpc.UserManagementServiceImplBase {

    private final UserService userService;

    public UserManagementServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<UserListResponse> responseObserver) {
        UserListResponse response = userService.getPaginatedUsers(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addUser(AddUserRequest request, StreamObserver<ServiceResponse> responseObserver) {
        boolean success = userService.addUser(
                request.getUsername(),
                request.getPassword(),
                request.getRole(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),     
                request.getSex(),       
                request.getDateOfBirth()
        );
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Thêm user thành công" : "Thêm user thất bại (username đã tồn tại?)")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setUserActiveStatus(SetUserActiveRequest request, StreamObserver<ServiceResponse> responseObserver) {
        boolean success = userService.setUserActiveStatus(request.getUsername(), request.getIsActive());

        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Cập nhật trạng thái thành công" : "Cập nhật thất bại (không tìm thấy user?)")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}