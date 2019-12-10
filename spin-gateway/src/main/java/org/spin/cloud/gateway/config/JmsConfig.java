package org.spin.cloud.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

/**
 * JMS监听容器配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
@EnableJms
public class JmsConfig {

    /**
     * topic模式的ListenerContainer
     */
    @Bean
    public JmsListenerContainerFactory jmsListenerContainerTopic(ConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setPubSubDomain(true);
        bean.setConnectionFactory(activeMQConnectionFactory);
        return bean;
    }

    /**
     * queue模式的ListenerContainer
     */
    @Bean
    public JmsListenerContainerFactory jmsListenerContainerQueue(ConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setConnectionFactory(activeMQConnectionFactory);
        return bean;
    }
}
