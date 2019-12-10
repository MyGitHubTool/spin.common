package org.spin.cloud.gateway.config;

import org.spin.cloud.gateway.service.DynamicRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 动态路由初始化
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class DynamicRouteDefinitionInitializer implements ApplicationRunner, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(DynamicRouteDefinitionInitializer.class);

    private final DynamicRouteService dynamicRouteService;

    @Autowired
    public DynamicRouteDefinitionInitializer(DynamicRouteService dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("系统启动，初始化动态路由信息");
        dynamicRouteService.reload();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
