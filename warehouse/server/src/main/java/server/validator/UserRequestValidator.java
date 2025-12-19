package server.validator;

import com.group9.warehouse.grpc.AddUserRequest;
import com.group9.warehouse.grpc.GetUsersRequest;
import com.group9.warehouse.grpc.SetUserActiveRequest;
import server.exception.ValidationException;

import java.util.regex.Pattern;

public class UserRequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+84|0)[0-9]{9,10}$");

    public static void validateAddUser(AddUserRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username không được để trống");
        }
        if (req.getUsername().length() < 4) {
            throw new ValidationException("Username phải có ít nhất 4 ký tự");
        }
        if (req.getPassword() == null || req.getPassword().length() < 8) {
            throw new ValidationException("Mật khẩu phải có ít nhất 8 ký tự");
        }
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

    public static void validateUpdateStatusUser (SetUserActiveRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username khong được để trống");
        }
    }

    public static void validateGetUsersRequest(GetUsersRequest req) {
        if (req.getPage() <= 0) {
            throw new ValidationException("Page phải lớn hơn 0");
        }
        if (req.getPageSize() <= 0) {
            throw new ValidationException("PageSize phải lớn hơn 0");
        }

    }
}
