package server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.group9.warehouse.grpc.UpdateProfileRequest;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.datasource.UserDataSource;
import server.model.User;
import server.repository.UserRepository;

import java.util.Date;
import java.util.Optional;

public class AuthService {

    public static final String JWT_SECRET = "DayLaMotBiMatRatRatLao";
    public static final Algorithm JWT_ALGORITHM = Algorithm.HMAC256(JWT_SECRET);
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> validateUser(String username, String plainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.info("AuthService/Login: Username {} wasn't existed", username);
            return Optional.empty();
        }
        User user = userOptional.get();

        if (!user.isActive()) {
            log.info("AuthService/Login:  Your account {} is banned.", username);
            return Optional.empty();
        }

        if (!BCrypt.checkpw(plainPassword, user.getHashedPassword())) {
            log.info("AuthService/Login : Password is not correct.");
            return Optional.empty();
        }
        return userOptional;
    }

    public Optional<User> getUserProfile(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean updateUserProfile(String username, UpdateProfileRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.info("AuthService/updateUserProfile: Username {} wasn't existed.", username);
            return false;
        }
        User user = userOptional.get();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSex(request.getSex());
        user.setDateOfBirth(request.getDateOfBirth());
        log.info("AuthService/updateUserProfile: User profile has been updated successfully.");
        return userRepository.update(user);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.info("AuthService/changePassword : Username {} wasn't existed.", username);
            return false;
        }
        User user = userOptional.get();

        if (!BCrypt.checkpw(oldPassword, user.getHashedPassword())) {
            log.info("AuthService/changePassword : Old password is not correct.");
            return false;
        }

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setHashedPassword(newHashedPassword);
        log.info("AuthService/changePassword : Password has been changed successfully.");
        return userRepository.update(user);
    }

    public String generateJwtToken(User user) {
        long EXPIRATION_TIME_MS = 86_400_000;
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("role", user.getRole())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(JWT_ALGORITHM);
    }
}