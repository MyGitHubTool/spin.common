package org.spin.cloud.gateway.config;


import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文档聚合提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
@Primary
public class GatewaySwaggerProvider implements SwaggerResourcesProvider {
    public static final String API_URI = "/v2/api-docs";
    private final RouteLocator routeLocator;
    private final GatewayProperties gatewayProperties;
    private final RouteDefinitionLocator routeDefinitionLocator;

    public GatewaySwaggerProvider(RouteLocator routeLocator, GatewayProperties gatewayProperties, RouteDefinitionLocator routeDefinitionLocator) {
        this.routeLocator = routeLocator;
        this.gatewayProperties = gatewayProperties;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        Set<String> routes = new HashSet<>();
        routeLocator.getRoutes().filter(route -> !route.getId().equals("bnd-gateway")).subscribe(route -> routes.add(route.getId()));
        routeDefinitionLocator.getRouteDefinitions().filter(definition -> routes.contains(definition.getId())).subscribe(routeDefinition -> routeDefinition.getPredicates().stream()
            .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()) && predicateDefinition.getArgs().containsKey("pattern"))
            .forEach(predicateDefinition -> resources.add(swaggerResource(routeDefinition.getId(),
                predicateDefinition.getArgs().get("pattern").replace("/**", API_URI)))));

        gatewayProperties.getRoutes().stream()
            .filter(routeDefinition -> routes.contains(routeDefinition.getId()))
            .forEach(routeDefinition -> routeDefinition.getPredicates().stream()
                .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                .forEach(predicateDefinition -> resources.add(swaggerResource(routeDefinition.getId(),
                    predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI)))));
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}
