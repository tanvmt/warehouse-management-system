package server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.group9.warehouse.grpc.UpdateProfileRequest;
import org.mindrot.jbcrypt.BCrypt;
import server.model.User;
import server.repository.UserRepository;

import java.util.Date;
import java.util.Optional;

public class AuthService {

    // Đây là bí mật, không được hardcode. Nên đọc từ file config.
    public static final String JWT_SECRET = "DayLaMotBiMatRatRatLao";
    public static final Algorithm JWT_ALGORITHM = Algorithm.HMAC256(JWT_SECRET);

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Logic đăng nhập MỚI
    public Optional<User> validateUser(String username, String plainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 1. Kiểm tra tài khoản có bị khóa không
            if (!user.isActive()) {
                System.out.println("Đăng nhập thất bại: Tài khoản " + username + " đã bị khóa.");
                return Optional.empty();
            }

            // 2. Kiểm tra mật khẩu
            if (BCrypt.checkpw(plainPassword, user.getHashedPassword())) {
                return userOptional; // Thành công
            }
        }
        return Optional.empty(); // Sai tên hoặc sai pass
    }

    public String generateJwtToken(User user) {
        long EXPIRATION_TIME_MS = 86_400_000; // 1 ngày
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("role", user.getRole())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(JWT_ALGORITHM);
    }

    // --- Logic Profile MỚI ---

    public Optional<User> getUserProfile(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean updateUserProfile(String username, UpdateProfileRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        // Cập nhật các trường
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSex(request.getSex());
        user.setDateOfBirth(request.getDateOfBirth());

        return userRepository.update(user); // Lưu thay đổi
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();

        // Kiểm tra pass cũ
        if (!BCrypt.checkpw(oldPassword, user.getHashedPassword())) {
            return false; // Mật khẩu cũ không đúng
        }

        // Băm và set pass mới
        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setHashedPassword(newHashedPassword);

        return userRepository.update(user); // Lưu thay đổi
    }
}