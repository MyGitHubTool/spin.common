package org.spin.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 内部访问白名单
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class InternalWhiteList {
    private static final Logger logger = LoggerFactory.getLogger(InternalWhiteList.class);

    private Set<String> hosts = new HashSet<>();
    private long updateTime;
    private DiscoveryClient discoveryClient;
    private final Object lock = new Object();

    private static final Set<String> whiteList = new HashSet<>();

    public InternalWhiteList(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        updateHosts();
    }

    public static void refreshList(String hosts) {
        synchronized (whiteList) {
            whiteList.clear();
            Arrays.stream(StringUtils.trimToEmpty(hosts).split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::toLowerCase)
                .forEach(whiteList::add);
        }
    }

    public boolean contains(String host) {
        updateHosts();
        String h = StringUtils.trimToEmpty(host).toLowerCase();
        return whiteList.contains(h) || hosts.contains(h);
    }

    public boolean containsOne(String... hosts) {
        updateHosts();
        for (String host : hosts) {
            String h = StringUtils.trimToEmpty(host).toLowerCase();
            if (whiteList.contains(h) || this.hosts.contains(h)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExpired() {
        return System.currentTimeMillis() - 120_000L > updateTime;
    }

    private void updateHosts() {
        if (null == discoveryClient) {
            return;
        }
        if (isExpired()) {
            synchronized (lock) {
                if (isExpired()) {
                    logger.info("更新服务实例信息");
                    hosts.clear();
                    try {
                        hosts.addAll(discoveryClient.getServices().stream().flatMap(i -> discoveryClient.getInstances(i).stream()).map(ServiceInstance::getHost).collect(Collectors.toSet()));
                    } catch (Exception ignore) {
                        // do nothing
                    }
                    hosts.add("localhost");
                    hosts.add("127.0.0.1");
                    hosts.add("0:0:0:0:0:0:0:1");
                    updateTime = System.currentTimeMillis();
                }
            }
        }
    }
}
