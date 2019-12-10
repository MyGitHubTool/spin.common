package org.spin.cloud.gateway;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 服务网关
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@EnableApolloConfig
@EnableTransactionManagement(proxyTargetClass = true)
@EnableDiscoveryClient
@SpringBootApplication
public class SpinGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpinGatewayApplication.class, args);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return (registry) -> registry.config().commonTags("application", applicationName);
    }
}
