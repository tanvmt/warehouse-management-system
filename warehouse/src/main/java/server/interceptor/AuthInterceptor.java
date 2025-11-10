package server.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.grpc.*;
import server.service.AuthService;

import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger; // Thêm import này
import org.slf4j.LoggerFactory; // Thêm import này

public class AuthInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    // Metadata key để client gửi token lên
    public static final Metadata.Key<String> AUTH_TOKEN_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    // Context key để lưu thông tin user sau khi giải mã
    public static final Context.Key<String> USERNAME_CONTEXT_KEY = Context.key("username");
    public static final Context.Key<String> ROLE_CONTEXT_KEY = Context.key("role");

    // Danh sách các hàm KHÔNG cần đăng nhập
    private static final Set<String> PUBLIC_METHODS = Set.of(
            "AuthService/Login"
    );

    // Danh sách các hàm CHỈ Manager được dùng
    private static final Set<String> MANAGER_METHODS = Set.of(
            "UserManagementService/GetUsers",
            "UserManagementService/AddUser",
            "UserManagementService/SetUserActiveStatus",
            "ProductManagementService/AddProduct",
            "ProductManagementService/UpdateProduct",
            "ProductManagementService/SetProductActiveStatus"
    );

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        logger.info("Intercepting call to: {}", methodName);

        // 1. Cho phép truy cập public (Login)
        if (PUBLIC_METHODS.contains(methodName)) {
            return next.startCall(call, headers);
        }

        // 2. Các hàm còn lại -> Yêu cầu Token
        String token = headers.get(AUTH_TOKEN_KEY);
        if (token == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Thiếu token (Authorization header)"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        // Thường token sẽ là "Bearer <token>". Phải loại bỏ "Bearer "
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 3. Giải mã Token
            JWTVerifier verifier = JWT.require(AuthService.JWT_ALGORITHM).build();
            DecodedJWT jwt = verifier.verify(token);

            String username = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            if (username == null || role == null) {
                throw new JWTVerificationException("Token không hợp lệ");
            }

            // 4. Ủy quyền (Authorization) - Kiểm tra vai trò
            if (MANAGER_METHODS.contains(methodName) && !"Manager".equals(role)) {
                call.close(Status.PERMISSION_DENIED.withDescription("Chỉ 'Manager' mới có quyền truy cập"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            // 5. Token hợp lệ, role hợp lệ -> Gắn thông tin vào Context
            Context context = Context.current()
                    .withValue(USERNAME_CONTEXT_KEY, username)
                    .withValue(ROLE_CONTEXT_KEY, role);

            return Contexts.interceptCall(context, call, headers, next);

        } catch (JWTVerificationException e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Token không hợp lệ hoặc hết hạn: " + e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}