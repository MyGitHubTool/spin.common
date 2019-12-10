package org.spin.cloud.gateway.config;

import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.*;


/**
 * 跨域配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
public class CorsConfig {
    private static final String ALL = "*";
    private static final String MAX_AGE = "18000L";

    private final Set<String> excludeService = new HashSet<>();

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (!CorsUtils.isCorsRequest(request)) {
                return chain.filter(exchange);
            }
            ServerHttpResponse response = exchange.getResponse();

            if (request.getMethod() == HttpMethod.OPTIONS) {
                addCorsHeaders(request, response);
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            String path = StringUtils.trimToEmpty(request.getPath().toString());
            String appId = "";
            if (StringUtils.isNotEmpty(path) && path.length() > 1 && path.lastIndexOf('/') != 0) {
                try {
                    appId = path.toLowerCase().substring(1, path.indexOf("/", 1));
                } catch (Exception ignore) {
                    // do nothing
                }
            }

            if (!excludeService.contains(appId)) {
                addCorsHeaders(request, response);
            }
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (!response.getHeaders().containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
                    addCorsHeaders(request, response);
                }
            }));
        };
    }

    @Value("${gateway.cors.exclude}")
    public void refreshExcludeService(String excludeService) {
        synchronized (this.excludeService) {
            this.excludeService.clear();
            this.excludeService.addAll(Arrays.asList(StringUtils.trimToEmpty(excludeService).toLowerCase().split(",")));
        }
    }

    private void addCorsHeaders(ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders requestHeaders = request.getHeaders();
        HttpHeaders responseHeaders = response.getHeaders();

        if (responseHeaders.getClass().getSimpleName().equals("ReadOnlyHttpHeaders")) {
            return;
        }

        responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL);
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
        List<String> headers = new LinkedList<>();
        headers.add(HttpHeaders.CONTENT_TYPE);
        headers.add(HttpHeaders.AUTHORIZATION);
        requestHeaders.getAccessControlRequestHeaders().forEach(h -> {
            switch (h.toLowerCase().trim()) {
                case "content-type":
                case "authorization":
                case "accept":
                case "accept-language":
                case "content-language":
                    break;
                default:
                    headers.add(h);
            }
        });

        responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.join(headers, ","));
        HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
        if (requestMethod != null) {
            responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
        }
    }
}
