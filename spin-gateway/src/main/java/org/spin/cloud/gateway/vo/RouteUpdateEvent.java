package org.spin.cloud.gateway.vo;

import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * 动态路由变更事件，用来触发集群的路由更新与同步
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RouteUpdateEvent {
    /**
     * 事件类型：0-重新加载 1-新增 2-更新 3-删除
     */
    private int opType;

    private String routeId;

    private RouteDefinition routeDefinition;

    public static RouteUpdateEvent forReload() {
        RouteUpdateEvent event = new RouteUpdateEvent();
        event.setOpType(0);
        return event;
    }

    public static RouteUpdateEvent forAdd(RouteDefinition definition) {
        RouteUpdateEvent event = new RouteUpdateEvent();
        event.setOpType(1);
        event.setRouteDefinition(definition);
        return event;
    }

    public static RouteUpdateEvent forUpdate(RouteDefinition definition) {
        RouteUpdateEvent event = new RouteUpdateEvent();
        event.setOpType(2);
        event.setRouteDefinition(definition);
        return event;
    }

    public static RouteUpdateEvent forDelete(String routeId) {
        RouteUpdateEvent event = new RouteUpdateEvent();
        event.setOpType(3);
        event.setRouteId(routeId);
        return event;
    }

    public int getOpType() {
        return opType;
    }

    private void setOpType(int opType) {
        this.opType = opType;
    }

    public String getRouteId() {
        return routeId;
    }

    private void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public RouteDefinition getRouteDefinition() {
        return routeDefinition;
    }

    private void setRouteDefinition(RouteDefinition routeDefinition) {
        this.routeDefinition = routeDefinition;
    }
}
