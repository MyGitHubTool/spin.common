package org.spin.cloud.gateway.service;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 动态路由Service
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface DynamicRouteService {

    /**
     * 查询所有动态路由信息
     *
     * @return 动态路由信息
     */
    Flux<RouteDefinition> getRoutes();

    /**
     * 查询指定的动态路由信息
     *
     * @param routeId 动态路由ID
     * @return 动态路由信息
     */
    Mono<RouteDefinition> getRoute(String routeId);

    /**
     * 发送动态路由刷新事件
     */
    void sendRouteReloadEvent();

    /**
     * 发送动态路由新增事件
     *
     * @param definition 动态路由定义
     */
    @Transactional
    void sendRouteAddEvent(RouteDefinition definition);

    /**
     * 发送动态路由更新事件
     *
     * @param definition 动态路由定义
     */
    @Transactional
    void sendRouteUpdateEvent(RouteDefinition definition);

    /**
     * 发送动态路由删除事件
     *
     * @param routeId 动态路由ID
     */
    @Transactional
    void sendRouteDeleteEvent(String routeId);

    /**
     * 重新加载动态路由
     */
    void reload();
}
