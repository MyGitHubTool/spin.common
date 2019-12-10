package org.spin.cloud.gateway.config;

import org.spin.cloud.gateway.vo.WhiteListInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
public class ServiceDiscoveryConfig {

    @Bean
    @Autowired
    public WhiteListInfo serviceDiscoveryInfo(DiscoveryClient discoveryClient) {
        return new WhiteListInfo(discoveryClient);
    }
}
