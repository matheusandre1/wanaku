package ai.wanaku.core.capabilities.common;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.trace.Span;
import io.quarkus.grpc.GlobalInterceptor;

@GlobalInterceptor
public class TracingServerInterceptor implements ServerInterceptor {

    private static final Logger LOG = Logger.getLogger(TracingServerInterceptor.class);

    static final String MDC_REQUEST_ID_KEY = "requestId";

    static final String SPAN_ATTR_REQUEST_ID = "wanaku.mcp.request_id";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String requestId = headers.get(Metadata.Key.of("x-wanaku-request-id", Metadata.ASCII_STRING_MARSHALLER));

        if (requestId != null && !requestId.isEmpty()) {
            withRequestId(requestId, () -> {
                try {
                    Span.current().setAttribute(SPAN_ATTR_REQUEST_ID, requestId);
                } catch (Exception e) {
                    LOG.tracef(e, "Could not set span attribute");
                }
            });
        }

        return new ServerCallListener<>(next.startCall(call, headers), requestId);
    }

    static void withRequestId(String requestId, Runnable action) {
        if (requestId != null && !requestId.isEmpty()) {
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
        }
        try {
            action.run();
        } finally {
            if (requestId != null && !requestId.isEmpty()) {
                MDC.remove(MDC_REQUEST_ID_KEY);
            }
        }
    }

    static class ServerCallListener<ReqT> extends ServerCall.Listener<ReqT> {
        private final ServerCall.Listener<ReqT> delegate;
        private final String requestId;

        ServerCallListener(ServerCall.Listener<ReqT> delegate, String requestId) {
            this.delegate = delegate;
            this.requestId = requestId;
        }

        @Override
        public void onMessage(ReqT message) {
            withRequestId(requestId, () -> delegate.onMessage(message));
        }

        @Override
        public void onHalfClose() {
            withRequestId(requestId, delegate::onHalfClose);
        }

        @Override
        public void onCancel() {
            withRequestId(requestId, delegate::onCancel);
        }

        @Override
        public void onComplete() {
            withRequestId(requestId, delegate::onComplete);
        }

        @Override
        public void onReady() {
            delegate.onReady();
        }
    }
}
