package org.spin.cloud.gateway.filter;

import org.reactivestreams.Publisher;
import org.spin.cloud.gateway.vo.RestfulResponse;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Response错误解析过滤器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/4/1</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class ErrorResponseResolveFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {

            @Override
            public Mono<Void> writeWith(@NonNull final Publisher<? extends DataBuffer> body) {
                if (null != getStatusCode() && body instanceof Flux) {
                    switch (getStatusCode()) {
                        case NOT_FOUND:
                        case BAD_GATEWAY:
                        case SERVICE_UNAVAILABLE:
                        case GATEWAY_TIMEOUT:
                        case METHOD_NOT_ALLOWED:
                            throw new ResponseStatusException(getStatusCode());
                        default:
                           /* if (getStatusCode().is3xxRedirection()) {
                                throw new SimplifiedException(ErrorCode.INTERNAL_ERROR, "不允许客户端跳转", new ResponseStatusException(getStatusCode()));
                            } else if (!getStatusCode().is2xxSuccessful()) {
                                throw new SimplifiedException(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDesc(), new ResponseStatusException(getStatusCode()));
                            } else {
                                setStatusCode(HttpStatus.OK);
                            }*/
                            if (getStatusCode().is4xxClientError()) {
                                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, ErrorCode.NETWORK_EXCEPTION.getDesc(), new ResponseStatusException(getStatusCode()));
                            } else if (getStatusCode().is5xxServerError()){
                                throw new SimplifiedException(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDesc(), new ResponseStatusException(getStatusCode()));
                            } else if (getStatusCode().is3xxRedirection()) {
                                // 临时开 将来关闭
                                setStatusCode(HttpStatus.FOUND);
                            } else {
                                setStatusCode(HttpStatus.OK);
                            }
                    }
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private DataBuffer fail(DataBufferFactory dataBufferFactory, String path, int errorCode, String... message) {
        RestfulResponse<Object> error = RestfulResponse.error(ErrorCode.INTERNAL_ERROR);
        error.setPath(path);
        error.setStatus(errorCode);
        if (message != null && message.length > 0 && StringUtils.isNotEmpty(message[0])) {
            error.setMessage(message[0]);
        }
        return dataBufferFactory.wrap(StringUtils.getBytesUtf8(JsonUtils.toJson(error)));
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
