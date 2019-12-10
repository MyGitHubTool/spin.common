package org.spin.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.spin.common.vo.CurrentUser;
import org.spin.common.web.interceptor.UserAuthInterceptor;
import org.spin.core.util.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign拦截器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/25</p>
 *
 * @author xuweinan
 * @author YIJIUE
 * @version 1.0
 */
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        //添加来自feign请求标识
        template.header(UserAuthInterceptor.FROM_FEIGN, UserAuthInterceptor.FROM_FEIGN);

        if (null != CurrentUser.getCurrent()) {
            template.header(HttpHeaders.FROM, StringUtils.urlEncode(CurrentUser.getCurrent().toString()).replaceFirst("%3A", ":"));
        }
        //添加token
        ServletRequestAttributes request = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (request != null) {
            String accessToken = request.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.isEmpty(accessToken)) {
                template.header(HttpHeaders.AUTHORIZATION, accessToken);
            }
        }
    }
}

