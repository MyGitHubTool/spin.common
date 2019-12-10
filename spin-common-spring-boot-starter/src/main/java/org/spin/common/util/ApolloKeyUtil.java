package org.spin.common.util;

import java.text.MessageFormat;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

/**
 * apollo国际化配置文件
 *
 * @author Merlin
 *
 */
@Configuration
@EnableApolloConfig
public class ApolloKeyUtil implements InitializingBean {

    private static Config enPublicConfig = ConfigService.getAppConfig();// apollo 总创建的命名空间名字

    private static ApolloKeyUtil apolloMessage = null;

    public static ApolloKeyUtil getApolloMessage() {
        return apolloMessage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        apolloMessage = this;
    }

    public static String getString(String key, Object[] params) {
        String msg = null;
        if (!StringUtils.isEmpty(key)) {

            String messageKey = enPublicConfig.getProperty(key, "");
            msg = MessageFormat.format(messageKey, params);
        }
        return msg;
    }

    public static String getString(String key) {
        String msg = null;
        if (!StringUtils.isEmpty(key)) {

            msg = enPublicConfig.getProperty(key, "");
        }
        return msg;
    }

    public static String getString(Config config ,String key) {
        String msg = null;
        if (!StringUtils.isEmpty(key)) {

            msg = config.getProperty(key, "");
        }
        return msg;
    }
}
