package server.service;

import com.group9.warehouse.grpc.GetUsersRequest;
import com.group9.warehouse.grpc.UserListResponse;
import com.group9.warehouse.grpc.PaginationInfo;
import org.mindrot.jbcrypt.BCrypt;
import server.grpc.AuthServiceImpl;
import server.model.User;
import server.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserListResponse getPaginatedUsers(GetUsersRequest request) {
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 10 : request.getPageSize();
        String searchTerm = request.getSearchTerm();
        Boolean isActive = request.hasIsActive() ? request.getIsActive().getValue() : null;

        List<User> users = userRepository.getPaginatedUsers(searchTerm, isActive, page, pageSize);
        long totalElements = userRepository.countUsers(searchTerm, isActive);
        long totalPages = (long) Math.ceil((double) totalElements / pageSize);

        PaginationInfo pagination = PaginationInfo.newBuilder()
                .setPageNumber(page)
                .setPageSize(pageSize)
                .setTotalElements(totalElements)
                .setTotalPages((int) totalPages)
                .build();

        UserListResponse.Builder responseBuilder = UserListResponse.newBuilder();
        responseBuilder.setPagination(pagination);

        // Convert model User sang gRPC UserProfile
        for (User u : users) {
            responseBuilder.addUsers(AuthServiceImpl.convertUserToProfile(u)); // Dùng chung hàm convert
        }

        return responseBuilder.build();
    }

    public boolean addUser(String username, String password, String role, String fullName, String email) {
        if (userRepository.existsByUsername(username)) {
            return false; // Đã tồn tại
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, hashedPassword, role, fullName, email, true); // Mặc định là active

        return userRepository.save(newUser);
    }

    public boolean setUserActiveStatus(String username, boolean isActive) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        user.setActive(isActive);
        return userRepository.update(user);
    }
}