package server.interceptor;

import io.grpc.*;
import server.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandlerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandlerInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    handleException(call, e);
                }
            }
        };
    }

    private void handleException(ServerCall<?, ?> call, Exception e) {
        Status status;

        if (e instanceof ResourceNotFoundException) {
            status = Status.NOT_FOUND.withDescription(e.getMessage());
        }
        else if (e instanceof ResourceAlreadyExistsException) {
            status = Status.ALREADY_EXISTS.withDescription(e.getMessage());
        }
        else if (e instanceof ValidationException) {
            status = Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }
        else if (e instanceof AuthenticationException) {
            status = Status.UNAUTHENTICATED.withDescription(e.getMessage());
        }
        else if (e instanceof AuthorizationException) {
            status = Status.PERMISSION_DENIED.withDescription(e.getMessage());
        }
        else {
            status = Status.INTERNAL.withDescription("Lỗi hệ thống: " + e.getMessage());
            log.error("Lỗi không xác định (Unhandled Exception): ", e);
        }

        call.close(status, new Metadata());
    }
}