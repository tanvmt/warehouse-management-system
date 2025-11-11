package server.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.service.AuthService;

import java.util.Objects;
import java.util.Set;

public class AuthInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> AUTH_TOKEN_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    public static final Context.Key<String> USERNAME_CONTEXT_KEY = Context.key("username");
    public static final Context.Key<String> ROLE_CONTEXT_KEY = Context.key("role");

    private static final Set<String> PUBLIC_METHODS = Set.of(
            "AuthService/Login"
    );

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
        log.info("AuthInterceptor: interceptCall: methodName = {}", methodName);

        if (PUBLIC_METHODS.contains(methodName)) {
            return next.startCall(call, headers);
        }

        String token = headers.get(AUTH_TOKEN_KEY);
        if (token == null) {
            log.error("Thieu token");
            call.close(Status.UNAUTHENTICATED.withDescription("Thiếu token (Authorization header)"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            JWTVerifier verifier = JWT.require(AuthService.JWT_ALGORITHM).build();
            DecodedJWT jwt = verifier.verify(token);

            String username = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            if (username == null || role == null) {
                log.error("token khong hop le");
                throw new JWTVerificationException("Token không hợp lệ");
            }

            if (MANAGER_METHODS.contains(methodName) && !"Manager".equals(role)) {
                log.error("Chi manager moi co quyen truy cap");
                call.close(Status.PERMISSION_DENIED.withDescription("Chỉ 'Manager' mới có quyền truy cập"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            Context context = Context.current()
                    .withValue(USERNAME_CONTEXT_KEY, username)
                    .withValue(ROLE_CONTEXT_KEY, role);

            return Contexts.interceptCall(context, call, headers, next);

        } catch (JWTVerificationException e) {
            log.error("Token khong hop le: {}", e.getMessage());
            call.close(Status.UNAUTHENTICATED.withDescription("Token không hợp lệ hoặc hết hạn: " + e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}