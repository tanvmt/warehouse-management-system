package server.service;


import com.group9.warehouse.grpc.UpdateProfileRequest;
import com.group9.warehouse.grpc.UserProfile;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.exception.AuthenticationException;
import server.exception.AuthorizationException;
import server.exception.ResourceNotFoundException;

import server.mapper.UserMapper;
import server.model.User;
import server.repository.UserRepository;


public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public User validateUser(String username, String plainPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (!user.isActive()) {
            throw new AuthorizationException("Tài khoản đã bị khóa, vui lòng liên hệ Admin");
        }

        if (!BCrypt.checkpw(plainPassword, user.getHashedPassword())) {
            throw new AuthenticationException("Sai mật khẩu");
        }
        return user;
    }

    public UserProfile getUserProfile(String username) {
        User user =  userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        return userMapper.convertUserToProfile(user);
    }

    public boolean updateUserProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSex(request.getSex());
        user.setDateOfBirth(request.getDateOfBirth());
        log.info("AuthService/updateUserProfile: User profile has been updated successfully.");
        return userRepository.update(user);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (!BCrypt.checkpw(oldPassword, user.getHashedPassword())) {
            throw new AuthenticationException("Mật khẩu cũ không chính xác");
        }

        user.setHashedPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        if (!userRepository.update(user)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể đổi mật khẩu");
        }
    }

}