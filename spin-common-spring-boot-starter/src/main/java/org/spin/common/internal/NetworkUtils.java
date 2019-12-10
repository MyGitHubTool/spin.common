package org.spin.common.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SpinException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import sun.net.util.IPAddressUtil;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网络工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class NetworkUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
    private static volatile long updateTime;
    private static final Set<String> networks = new HashSet<>();
    private static final Set<Short> v4NetMasks = new HashSet<>();
    private static final Set<Short> v6NetMasks = new HashSet<>();
    private static final Object lock = new Object();

    private static Set<String> hosts = new HashSet<>();
    private static DiscoveryClient discoveryClient;

    static {
        try {
            List<NetAddress> networkInfo = getNetworkInfo();
            networkInfo.forEach(it -> {
                if (it.isIpV4()) {
                    v4NetMasks.add(it.getNetMask());
                } else if (it.isIpV6()) {
                    v6NetMasks.add(it.getNetMask());
                }
                networks.add(it.getAddress().getHostAddress());
            });
            networks.add("localhost");
            networks.add("127.0.0.1");
            networks.add("0:0:0:0:0:0:0:1");
            hosts.addAll(networks);
        } catch (Exception ignore) {
            // do nothing
        }
    }

    public static List<NetworkInterface> getNetworkInterfaces() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new SpinException("获取本机网卡异常", e);
        }

        List<NetworkInterface> res = new LinkedList<>();
        while (networkInterfaces.hasMoreElements()) {
            res.add(networkInterfaces.nextElement());
        }
        return res;
    }

    public static List<NetAddress> getNetworkInfo() {
        return getNetworkInterfaces().stream().filter(it -> {
            try {
                return !it.isLoopback();
            } catch (SocketException e) {
                return false;
            }
        }).flatMap(it -> it.getInterfaceAddresses().stream()).map(it -> {
            byte[] address = it.getAddress().getAddress();
            byte[] segment = new byte[address.length];
            int bytes = it.getNetworkPrefixLength() / 8;
            int bits = it.getNetworkPrefixLength() % 8;
            for (int i = 0; i < address.length; i++) {
                if (i < bytes) {
                    segment[i] = address[i];
                } else if (i == bytes) {
                    segment[i] = (byte) (address[i] >>> (8 - bits));
                } else {
                    segment[i] = 0;
                }
            }
            try {
                return new NetAddress(InetAddress.getByAddress(segment), it.getNetworkPrefixLength());
            } catch (UnknownHostException e) {
                throw new SpinException(e);
            }
        }).collect(Collectors.toList());
    }

    public static boolean contains(String host) {
        updateHosts();
        return hosts.contains(host.toLowerCase());
    }

    public static boolean inSameVlan(String hostName) {
        updateHosts();
        String host = hostName.toLowerCase();
        if (hosts.contains(host)) {
            return true;
        }

        try {
            byte[] address;
            Set<Short> netMasks;
            if (host.contains(":")) {
                address = IPAddressUtil.textToNumericFormatV6(host);
                netMasks = v6NetMasks;
            } else {
                address = IPAddressUtil.textToNumericFormatV4(host);
                netMasks = v4NetMasks;
            }

            for (Short it : netMasks) {
                byte[] segment = new byte[address.length];
                int bytes = it / 8;
                int bits = it % 8;
                for (int i = 0; i < address.length; i++) {
                    if (i < bytes) {
                        segment[i] = address[i];
                    } else if (i == bytes) {
                        segment[i] = (byte) (address[i] >>> (8 - bits));
                    } else {
                        segment[i] = 0;
                    }
                }
                String hostAddress = InetAddress.getByAddress(segment).getHostAddress();
                if (hosts.contains(hostAddress)) {
                    return true;
                }
            }

        } catch (Exception ignore) {
        }
        return false;
    }

    public static void setDiscoveryClient(DiscoveryClient discoveryClient) {
        NetworkUtils.discoveryClient = discoveryClient;
        updateHosts();
    }

    public static class NetAddress {
        private InetAddress address;
        private short netMask;

        public NetAddress(InetAddress address, short netMask) {
            this.address = address;
            this.netMask = netMask;
        }

        public InetAddress getAddress() {
            return address;
        }

        public void setAddress(InetAddress address) {
            this.address = address;
        }

        public short getNetMask() {
            return netMask;
        }

        public void setNetMask(short netMask) {
            this.netMask = netMask;
        }

        public boolean isIpV4() {
            return address instanceof Inet4Address;
        }

        public boolean isIpV6() {
            return address instanceof Inet6Address;
        }
    }

    private static boolean isExpired() {
        return System.currentTimeMillis() - 60_000L > updateTime;
    }

    private static void updateHosts() {
        if (null == discoveryClient) {
            return;
        }
        if (isExpired()) {
            synchronized (lock) {
                if (isExpired()) {
                    logger.info("更新服务实例信息");
                    hosts.clear();
                    hosts.addAll(networks);
                    hosts.addAll(discoveryClient.getServices().stream().flatMap(i -> discoveryClient.getInstances(i).stream()).map(ServiceInstance::getHost).map(String::toLowerCase).collect(Collectors.toSet()));
                    updateTime = System.currentTimeMillis();
                }
            }
        }
    }
}
