package org.spin.cloud.gateway.filter;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.gateway.vo.RestfulResponse;
import org.spin.cloud.gateway.vo.WhiteListInfo;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.security.Base64;
import org.spin.core.util.IOUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.SerializeUtils;
import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Token解析过滤器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
@EnableApolloConfig
public class TokenResolveFilter implements GlobalFilter {
    private static final Logger logger = LoggerFactory.getLogger(TokenResolveFilter.class);
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Read-IP";
    private static final String GATEWAY = "GATEWAY";

    private PublicKey publicKey;

    private final WhiteListInfo whiteListInfo;

    private Set<String> blackList;
    private static final Map<String, Long> dynamicBlackList = new ConcurrentHashMap<>();

    static {
        initBlackList();
    }

    @Autowired
    public TokenResolveFilter(WhiteListInfo whiteListInfo) {
        this.whiteListInfo = whiteListInfo;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        final String realIp = StringUtils.trimToNull(request.getHeaders().getFirst(X_REAL_IP));
        final String forwardedFor = StringUtils.trimToNull(request.getHeaders().getFirst(X_FORWARDED_FOR));
        final boolean hasProxyHead = StringUtils.isNotBlank(realIp);

        String source = realIp;
        if (null == source) {
            source = request.getHeaders().getFirst(X_FORWARDED_FOR);
            if (StringUtils.isNotEmpty(source)) {
                source = StringUtils.trimToNull(source.split(",")[0]);
            } else {
                source = null;
            }
        }

        if (null == source) {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (null != remoteAddress) {
                source = remoteAddress.getAddress().getHostAddress();
            }
        }

        String msg = "由于安全策略，您已被记录并禁止访问";
        if (null != blackList && blackList.contains(source)) {
            return error(response, RestfulResponse.error(ErrorCode.ACCESS_DENINED, msg));
        }

        if (dynamicBlackList.containsKey(source)) {
            long expiredSince = dynamicBlackList.get(source);
            if (expiredSince == 0 || expiredSince > System.currentTimeMillis()) {
                return error(response, RestfulResponse.error(ErrorCode.ACCESS_DENINED, msg));
            } else {
                dynamicBlackList.remove(source);
            }
        }

        StringBuilder from = new StringBuilder();
        String token = null;
        List<String> authorizations = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authorizations != null && !authorizations.isEmpty()) {
            token = authorizations.get(0);
        }
        if (StringUtils.isEmpty(token)) {
            HttpCookie cookie = request.getCookies().getFirst(HttpHeaders.AUTHORIZATION);
            if (null != cookie) {
                token = cookie.getValue();
            }
        }

        if (StringUtils.isNotEmpty(token)) {
            Claims claims;
            try {
                claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token)
                    .getBody();
                from.append(claims.getSubject());
            } catch (ExpiredJwtException ignore) {
                logger.warn("Token已过期: {}", token);
                from.append("-601:anonymous");
//                return error(response, ErrorCode.TOKEN_EXPIRED);
            } catch (Exception ignore) {
                logger.warn("无效的Token: {}", token);
                from.append("-602:anonymous");
//                return error(response, ErrorCode.TOKEN_INVALID);
            }
        }

        boolean inner = request.getRemoteAddress() != null && whiteListInfo.contains(request.getRemoteAddress().getHostName());

        String ip = source;
        request = request.mutate().headers(httpHeaders -> {
//            httpHeaders.remove(HttpHeaders.AUTHORIZATION);

            httpHeaders.remove(HttpHeaders.FROM);
            if (!hasProxyHead) {
                httpHeaders.set(X_REAL_IP, ip);
            }

            httpHeaders.set(X_FORWARDED_FOR, (null == forwardedFor ? ip : forwardedFor) + "," + GATEWAY);

            if (StringUtils.isNotEmpty(from)) {
                httpHeaders.set(HttpHeaders.FROM, StringUtils.urlEncode(from.toString()).replaceFirst("%3A", ":"));
            }
            List<String> referers = httpHeaders.get(HttpHeaders.REFERER);
            if (null == referers) {
                referers = new ArrayList<>(1);
            } else {
                referers = new ArrayList<>(referers);
            }
            referers.add(GATEWAY);
            String referer = StringUtils.join(referers, ";");
            httpHeaders.set(HttpHeaders.REFERER, referer);

        }).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Value("${tokenSecret}")
    public void setTokenSecret(String tokenSecret) {
        publicKey = SerializeUtils.deserialize(Base64.decode(tokenSecret));
        logger.info("Token Secret刷新成功");
    }

    @Value("${gateway.blackList:}")
    public void setConfigurableBlackList(String blackListStr) {
        String[] split = StringUtils.split(blackListStr, ",");
        if (null == split || split.length == 0) {
            if (null != blackList) {
                blackList.clear();
            }
            return;
        }
        blackList = Arrays.stream(split).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
    }

    public static void addToBlackList(String ip, long expiredIn) {
        Assert.notEmpty(ip, "添加黑名单必须指定地址");
        Assert.notTrue(expiredIn < 0, "有效期不能小于0");
        if (StringUtils.isNotBlank(ip)) {
            dynamicBlackList.put(ip.trim(), expiredIn != 0 ? (System.currentTimeMillis() + expiredIn) : expiredIn);
        }
    }

    public static void removeFromBlackList(String ip) {
        dynamicBlackList.remove(StringUtils.trimToEmpty(ip));
    }

    public static void initBlackList() {
        dynamicBlackList.clear();
        try (InputStream is = new FileInputStream(new File("/opt/settings/black.list"))) {
            String blackList = StringUtils.trimToEmpty(IOUtils.copyToString(is, StandardCharsets.UTF_8));
            Arrays.stream(blackList.split(",")).filter(StringUtils::isNotEmpty)
                .map(String::trim).forEach(it -> dynamicBlackList.put(it, 0L));
        } catch (Exception ignore) {
            // do nothing
        }
    }

    private Mono<Void> error(ServerHttpResponse response, RestfulResponse error) {
        DataBuffer dataBuffer = response.bufferFactory().wrap(StringUtils.getBytesUtf8(JsonUtils.toJson(error)));
        HttpHeaders headers = response.getHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        return response.writeAndFlushWith(Mono.just(Mono.just(dataBuffer)));
    }
}
