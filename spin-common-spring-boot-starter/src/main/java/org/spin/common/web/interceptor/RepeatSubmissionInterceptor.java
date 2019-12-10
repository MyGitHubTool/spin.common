package org.spin.common.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.redis.RedisUtil;
import org.spin.common.web.RestfulResponse;
import org.spin.common.web.annotation.RepeatSubmission;
import org.spin.core.ErrorCode;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 表单防重复提交拦截器
 *
 * @author aricXu
 * <p>Created by aricXu on 2019/7/29</p>
 */
public class RepeatSubmissionInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RepeatSubmissionInterceptor.class);
    private RedisUtil redisUtil;

    public RepeatSubmissionInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (null != redisUtil && handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmission annotation = method.getAnnotation(RepeatSubmission.class);
            if (annotation != null) {
                //获取请求地址
                String requestURI = request.getRequestURI();
                //获取ip地址 后面要改为获取真实的IP地址，这个地址很可能是代理服务器的地址
                String ipAddress = request.getRemoteAddr();
                //获取post的提交内容
                Map<String, String[]> parameterMap = request.getParameterMap();
                //拼接redis要存入的key
                StringBuilder key = new StringBuilder(requestURI);
                key.append(ipAddress).append(JsonUtils.toJson(parameterMap));
                try {
                    String value = redisUtil.getValue(key.toString());
                    if (null != value && value.length() > 0) {
                        responseWrite(response, ErrorCode.OTHER, "请不要重复提交");
                        return false;
                    }
                    redisUtil.setString(key.toString(), "1", annotation.limitTime(), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    logger.warn("重复表单校验失败", e);
                    // do nothing
                }
                return true;
            }
        }
        return true;
    }

    private void responseWrite(HttpServletResponse response, ErrorCode errorCode, String... message) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setHeader("Encoded", "1");
            response.getWriter().write(JsonUtils.toJson(RestfulResponse.error(errorCode, ((null == message || message.length == 0 || StringUtils.isEmpty(message[0])) ? errorCode.getDesc() : message[0]))));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
