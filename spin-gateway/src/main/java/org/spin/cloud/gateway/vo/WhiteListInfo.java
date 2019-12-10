package org.spin.cloud.gateway.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务白名单实例信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class WhiteListInfo {
    private static final Logger logger = LoggerFactory.getLogger(WhiteListInfo.class);

    private Set<String> hosts = new HashSet<>();
    private long updateTime;
    private DiscoveryClient discoveryClient;
    private final Object lock = new Object();

    public WhiteListInfo(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        updateHosts();
    }

    public boolean contains(String host) {
        updateHosts();
        return hosts.contains(host);
    }

    private boolean isExpired() {
        return System.currentTimeMillis() - 60_000L > updateTime;
    }

    private void updateHosts() {
        if (isExpired()) {
            synchronized (lock) {
                if (isExpired()) {
                    logger.info("更新服务实例信息");
                    hosts.clear();
                    hosts.addAll(discoveryClient.getServices().stream().flatMap(i -> discoveryClient.getInstances(i).stream()).map(ServiceInstance::getHost).collect(Collectors.toSet()));
                    hosts.add("localhost");
                    hosts.add("127.0.0.1");
                    hosts.add("0:0:0:0:0:0:0:1");
                    updateTime = System.currentTimeMillis();
                }
            }
        }
    }
}
