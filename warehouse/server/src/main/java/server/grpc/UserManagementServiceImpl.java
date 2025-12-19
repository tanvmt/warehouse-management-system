package server.grpc;

import com.group9.warehouse.grpc.*;
import io.grpc.stub.StreamObserver;
import server.service.UserService;
import server.validator.UserRequestValidator;

public class UserManagementServiceImpl extends UserManagementServiceGrpc.UserManagementServiceImplBase {

    private final UserService userService;

    public UserManagementServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<UserListResponse> responseObserver) {
        UserRequestValidator.validateGetUsersRequest(request);

        UserListResponse response = userService.getPaginatedUsers(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addUser(AddUserRequest request, StreamObserver<ServiceResponse> responseObserver) {
        UserRequestValidator.validateAddUser(request);

        userService.addUser(
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
                .setSuccess(true)
                .setMessage("Thêm user thành công")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setUserActiveStatus(SetUserActiveRequest request, StreamObserver<ServiceResponse> responseObserver) {
        UserRequestValidator.validateUpdateStatusUser(request);

        userService.setUserActiveStatus(request.getUsername(), request.getIsActive());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Cập nhật trạng thái thành công!")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}