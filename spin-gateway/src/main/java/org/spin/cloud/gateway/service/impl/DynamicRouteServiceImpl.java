package org.spin.cloud.gateway.service.impl;

import org.apache.activemq.command.ActiveMQTopic;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.gateway.repository.Tables;
import org.spin.cloud.gateway.repository.tables.records.GatewayRouteDefinitionRecord;
import org.spin.cloud.gateway.service.DynamicRouteService;
import org.spin.cloud.gateway.vo.RouteUpdateEvent;
import org.spin.core.Assert;
import org.spin.core.collection.FixedVector;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 动态路由Service实现
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Service
public class DynamicRouteServiceImpl implements DynamicRouteService, ApplicationEventPublisherAware {
    private static final Logger logger = LoggerFactory.getLogger(DynamicRouteServiceImpl.class);
    private static final String ROUTES_DEST = "bonade.cloud.gateway.routes";
    private static final String RELOAD_EVENT = JsonUtils.toJson(RouteUpdateEvent.forReload());

    private ApplicationEventPublisher publisher;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final JmsMessagingTemplate jmsTemplate;

    private final DSLContext dslContext;

    @Autowired
    public DynamicRouteServiceImpl(RouteDefinitionWriter routeDefinitionWriter,
                                   RouteDefinitionLocator routeDefinitionLocator,
                                   JmsMessagingTemplate jmsTemplate,
                                   DSLContext dslContext) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.jmsTemplate = jmsTemplate;
        if (null != this.jmsTemplate.getJmsTemplate()) {
            this.jmsTemplate.getJmsTemplate().setPubSubDomain(true);
        }
        this.dslContext = dslContext;
    }


    @JmsListener(destination = ROUTES_DEST, containerFactory = "jmsListenerContainerTopic")
    public void onDynamicRoutesMessage(TextMessage message) {
        logger.info("收到topic消息，更新路由");
        RouteUpdateEvent event;
        try {
            event = JsonUtils.fromJson(message.getText(), RouteUpdateEvent.class);
        } catch (JMSException ignore) {
            return;
        }
        switch (event.getOpType()) {
            case 0:
                logger.info("刷新路由");
                reload();
                break;
            case 1:
                logger.info("新增路由: {}", event.getRouteDefinition().getId());
                add(event.getRouteDefinition());
                break;
            case 2:
                logger.info("更新路由: {}", event.getRouteDefinition().getId());
                update(event.getRouteDefinition());
                break;
            case 3:
                logger.info("删除路由: {}", event.getRouteId());
                delete(event.getRouteId());
                break;
            default:
                // do nothing
        }
    }

    @Override
    public Flux<RouteDefinition> getRoutes() {
        return routeDefinitionLocator.getRouteDefinitions();
    }

    @Override
    public Mono<RouteDefinition> getRoute(String routeId) {
        return routeDefinitionLocator.getRouteDefinitions().filter(r -> r.getId().equals(routeId))
            .next().switchIfEmpty(Mono.error(new SimplifiedException("指定的路由ID[" + routeId + "]不存在")));
    }

    @Override
    public void sendRouteReloadEvent() {
        jmsTemplate.convertAndSend(new ActiveMQTopic(ROUTES_DEST), RELOAD_EVENT);
    }

    @Override
    @Transactional
    public void sendRouteAddEvent(RouteDefinition definition) {
        Assert.notNull(definition, "路由定义不能为空");

        FixedVector<Boolean> exists = new FixedVector<>(1);
        routeDefinitionLocator.getRouteDefinitions().any(r -> r.getId().equals(definition.getId())).subscribe(exists::put);
        if (exists.pop()) {
            throw new SimplifiedException("已经存在ID为[" + definition.getId() + "]的路由");
        }

        try {
            dslContext.insertInto(Tables.GATEWAY_ROUTE_DEFINITION)
                .set(GatewayRouteDefinitionRecord.fromRouteDefinition(definition))
                .execute();
        } catch (DuplicateKeyException ignore) {
            throw new SimplifiedException("指定的路由ID[" + definition.getId() + "]已经存在");
        }
        jmsTemplate.convertAndSend(new ActiveMQTopic(ROUTES_DEST), JsonUtils.toJson(RouteUpdateEvent.forAdd(definition)));
    }

    @Override
    @Transactional
    public void sendRouteUpdateEvent(RouteDefinition definition) {
        Assert.notNull(definition, "路由定义不能为空");
        int cnt = dslContext.update(Tables.GATEWAY_ROUTE_DEFINITION)
            .set(GatewayRouteDefinitionRecord.fromRouteDefinition(definition))
            .where(Tables.GATEWAY_ROUTE_DEFINITION.ID.eq(definition.getId()))
            .execute();

        Assert.notTrue(0 == cnt, "指定的路由不存在");
        jmsTemplate.convertAndSend(new ActiveMQTopic(ROUTES_DEST), JsonUtils.toJson(RouteUpdateEvent.forUpdate(definition)));
    }

    @Override
    @Transactional
    public void sendRouteDeleteEvent(String routeId) {
        Assert.notEmpty(routeId, "路由ID不能为空");
        int cnt = dslContext.delete(Tables.GATEWAY_ROUTE_DEFINITION)
            .where(Tables.GATEWAY_ROUTE_DEFINITION.ID.eq(routeId))
            .execute();
        Assert.notTrue(0 == cnt, "指定的路由不存在");
        jmsTemplate.convertAndSend(new ActiveMQTopic(ROUTES_DEST), JsonUtils.toJson(RouteUpdateEvent.forDelete(routeId)));
    }

    @Override
    public void reload() {

        List<GatewayRouteDefinitionRecord> fetch = dslContext.select().from(Tables.GATEWAY_ROUTE_DEFINITION).fetch().into(Tables.GATEWAY_ROUTE_DEFINITION);
        if (CollectionUtils.isEmpty(fetch)) {
            throw new SimplifiedException("没有定义任何动态路由");
        }
        routeDefinitionLocator.getRouteDefinitions().subscribe(it -> routeDefinitionWriter.delete(Mono.just(it.getId())));

        fetch.stream().map(GatewayRouteDefinitionRecord::toRouteDefinition).forEach(route -> routeDefinitionWriter.save(Mono.just(route)).subscribe());
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 增加路由
     *
     * @param definition 动态路由定义
     */
    private void add(RouteDefinition definition) {
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 更新路由
     *
     * @param definition 动态路由定义
     */
    private void update(RouteDefinition definition) {
        try {
            routeDefinitionWriter.delete(Mono.just(definition.getId())).subscribe();
        } catch (Exception ignore) {
            // do nothing
        }

        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            publisher.publishEvent(new RefreshRoutesEvent(this));
        } catch (Exception ignore) {
            // do nothing
        }
    }

    /**
     * 删除路由
     *
     * @param routeId 动态路由ID
     */
    private void delete(String routeId) {
        routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
