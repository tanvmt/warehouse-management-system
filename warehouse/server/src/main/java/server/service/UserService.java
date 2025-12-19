package server.service;

import com.group9.warehouse.grpc.GetUsersRequest;
import com.group9.warehouse.grpc.UserListResponse;
import com.group9.warehouse.grpc.PaginationInfo;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.exception.ResourceAlreadyExistsException;
import server.exception.ResourceNotFoundException;
import server.exception.ValidationException;
import server.grpc.AuthServiceImpl;
import server.mapper.UserMapper;
import server.model.User;
import server.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserMapper userMapper;
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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

        UserListResponse.Builder builder = UserListResponse.newBuilder().setPagination(pagination);
        users.forEach(u -> builder.addUsers(userMapper.convertUserToProfile(u)));

        return builder.build();
    }

    public void addUser(String username, String password, String role, String fullName, String email, String phone, String sex, String dateOfBirth) {
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username '" + username + "' đã tồn tại.");
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, hashedPassword, role, fullName, email, phone, sex, dateOfBirth, true);

        boolean isSaved = userRepository.save(newUser);
        if (!isSaved) {
            throw new RuntimeException("Lỗi hệ thống: Không thể lưu dữ liệu xuống file JSON.");
        }
        log.info("UserService/addUser : Add new user with username: {}", username);
    }

    public void setUserActiveStatus(String username, boolean isActive) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        user.setActive(isActive);

        boolean isSaved = userRepository.update(user);
        if (!isSaved) {
            throw new RuntimeException("Lỗi hệ thống: Không thể cập nhật dữ liệu.");
        }
    }
}