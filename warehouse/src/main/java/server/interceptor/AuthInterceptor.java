package server.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.service.AuthService;
import server.service.JwtService;

import java.util.Set;

public class AuthInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    public static final Metadata.Key<String> AUTH_TOKEN_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> USERNAME_CONTEXT_KEY = Context.key("username");
    public static final Context.Key<String> ROLE_CONTEXT_KEY = Context.key("role");

    private static final Set<String> PUBLIC_METHODS = Set.of(
            "auth.AuthService/Login"
    );

    private static final Set<String> MANAGER_METHODS = Set.of(
            "user.UserManagementService/GetUsers",
            "user.UserManagementService/AddUser",
            "user.UserManagementService/SetUserActiveStatus",
            "product.ProductManagementService/AddProduct",
            "product.ProductManagementService/UpdateProduct",
            "product.ProductManagementService/SetProductActiveStatus",
            "warehouse.WarehouseService/GetSummaryReport"
    );
    private JwtService jwtService;
    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("AuthInterceptor: Request đến method: {}", methodName);


        if (PUBLIC_METHODS.contains(methodName)) {
            return next.startCall(call, headers);
        }

        String token = headers.get(AUTH_TOKEN_KEY);
        if (token == null || token.isEmpty()) {
            log.warn("Từ chối truy cập: Thiếu token Authorization");
            call.close(Status.UNAUTHENTICATED.withDescription("Vui lòng đăng nhập (Thiếu Token)"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {

            DecodedJWT jwt = jwtService.verifyToken(token);

            String username = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            if (username == null || role == null) {
                throw new JWTVerificationException("Token thiếu thông tin quan trọng");
            }

            if (MANAGER_METHODS.contains(methodName) && !"Manager".equals(role)) {
                log.warn("Từ chối truy cập: User '{}' (Role: {}) cố truy cập API Manager", username, role);
                call.close(Status.PERMISSION_DENIED.withDescription("Chỉ 'Manager' mới có quyền truy cập chức năng này"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            Context context = Context.current()
                    .withValue(USERNAME_CONTEXT_KEY, username)
                    .withValue(ROLE_CONTEXT_KEY, role);

            return Contexts.interceptCall(context, call, headers, next);

        } catch (JWTVerificationException e) {
            log.error("Token không hợp lệ: {}", e.getMessage());
            call.close(Status.UNAUTHENTICATED.withDescription("Token không hợp lệ hoặc đã hết hạn"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}