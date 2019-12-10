package org.spin.cloud.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.netty.http.client.PrematureCloseException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/30</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GlobalExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                  ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 获取异常属性
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        int code = 500;
        String msg = ErrorCode.INTERNAL_ERROR.getDesc();
        String errorMsg = null;
        Throwable error = super.getError(request);
        if (logger.isWarnEnabled()) {
            logger.warn("服务调用失败: [{} {}]: {}", request.methodName(), request.path(), error.getMessage());
        }

        if (error instanceof NotFoundException) {
            code = 404;
            msg = "请求的资源不存在";
            errorMsg = HttpStatus.NOT_FOUND.toString();
            return response(code, msg, request.path(), errorMsg);
        } else if (error instanceof SimplifiedException) {
            code = ((SimplifiedException) error).getExceptionType().getCode();
            code = Math.max(code, 500);
            msg = ((SimplifiedException) error).getSimpleMessage();
            if (error.getCause() instanceof ResponseStatusException) {
                errorMsg = StringUtils.toString(((ResponseStatusException) error.getCause()).getStatus());
            }
            return response(code, msg, request.path(), errorMsg);
        } else if (error instanceof ConnectException) {
            if (error.getCause() instanceof ConnectException) {
                return response(801, "远程服务连接超时: " + error.getMessage().substring(error.getCause().getMessage().length() + 2), request.path(), null);
            } else {
                return response(801, "远程服务连接超时", request.path(), null);
            }
        } else if (error instanceof UnknownHostException) {
            return response(802, "无法解析服务主机: " + error.getMessage(), request.path(), null);
        } else if (error instanceof ResponseStatusException) {
            HttpStatus status = ((ResponseStatusException) error).getStatus();
            errorMsg = status.toString();

            if (status.value() == 404) {
                code = 404;
                msg = "请求的资源不存在";
            } else if (status.value() > 501 && status.value() < 505) {
                code = status.value();
                msg = "服务暂时不可用";
            } else if (status.value() == 405) {
                code = 405;
                HttpMethod method = request.method();
                msg = String.format("不支持的请求类型: %s [%s]", (method == null ? "Unknown" : method.name()), request.path());
            }
            return response(code, msg, request.path(), errorMsg);
        } else if (error instanceof PrematureCloseException) {
            code = ErrorCode.NETWORK_EXCEPTION.getCode();
            msg = "网络连接被关闭";
            errorMsg = error.getMessage();
            return response(code, msg, request.path(), errorMsg);
        }
        logger.warn("网关发生未处理异常", error);
        return response(code, msg, request.path(), error.getMessage());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        return HttpStatus.OK;
    }

    /**
     * 构建返回的JSON数据格式
     *
     * @param status  状态码
     * @param message 异常信息
     */
    private Map<String, Object> response(int status, String message, String path, String error) {
        Map<String, Object> map = new HashMap<>(8);
        map.put("timestamp", System.currentTimeMillis());
        map.put("status", status);
        map.put("path", path);
        map.put("error", error);
        map.put("message", message);
        map.put("data", null);
        return map;
    }
}
