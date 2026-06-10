package ai.wanaku.backend.bridge.transports.grpc;

import org.jboss.logging.MDC;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.quarkus.grpc.GlobalInterceptor;

@GlobalInterceptor
public class RequestIdClientInterceptor implements ClientInterceptor {

    static final Metadata.Key<String> REQUEST_ID_KEY =
            Metadata.Key.of("x-wanaku-request-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);

        String requestId = (String) MDC.get("requestId");
        if (requestId != null && !requestId.isEmpty()) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    headers.put(REQUEST_ID_KEY, requestId);
                    super.start(responseListener, headers);
                }
            };
        }

        return delegate;
    }
}
