package server.validator;

import com.group9.warehouse.grpc.AddUserRequest;
import com.group9.warehouse.grpc.ChangePasswordRequest;
import com.group9.warehouse.grpc.LoginRequest;
import com.group9.warehouse.grpc.UpdateProfileRequest;
import server.exception.ValidationException;

import java.util.regex.Pattern;

public class AuthRequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+84|0)[0-9]{9,10}$");

    public static void validateChangePassword(ChangePasswordRequest req) {
        if (req.getNewPassword() == null ||
                req.getOldPassword() == null ||
                req.getOldPassword().isEmpty() ||
                req.getNewPassword().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống");
        }
        if (req.getNewPassword().length() < 8) {
            throw new ValidationException("Mật khẩu mới phải có ít nhất 8 ký tự");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new ValidationException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }
    }

    public static void validateLogin(LoginRequest req) {
        if (req.getUsername() == null || req.getUsername().isEmpty()) {
            throw new ValidationException("Username không được để trống");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống");
        }

        if (req.getPassword().length() < 8) {
            throw new ValidationException("Mật khẩu không đúng định dạng");
        }
    }

    public static void validateUpdateUser (UpdateProfileRequest req) {

        if (req.getFullName() == null || req.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên không được để trống");
        }

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(req.getEmail()).matches()) {
                throw new ValidationException("Email không đúng định dạng");
            }
        }

        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            if (!PHONE_PATTERN.matcher(req.getPhone()).matches()) {
                throw new ValidationException("Số điện thoại không hợp lệ");
            }
        }

        if (req.getDateOfBirth() == null || req.getDateOfBirth().trim().isEmpty()) {
            throw new ValidationException("Ngày tháng năm sinh không được để trống");
        }

        if (req.getSex() == null || req.getSex().trim().isEmpty()) {
            throw new ValidationException("Giới tính không được để trống");
        }
    }
}
