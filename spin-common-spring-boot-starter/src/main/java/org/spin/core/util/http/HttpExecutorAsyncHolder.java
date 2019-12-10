package org.spin.core.util.http;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 异步Http客户端持有者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/7/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class HttpExecutorAsyncHolder {
    private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();
    private static volatile CloseableHttpAsyncClient httpAsyncClient;

    static CloseableHttpAsyncClient getClient() {
        return httpAsyncClient;
    }

    static void initAsync(int maxTotal, int maxPerRoute, byte[] certificate, String password, String algorithm) {
        if (null != certificate && StringUtils.isNotEmpty(algorithm)) {
            try (InputStream ignored = new ByteArrayInputStream(certificate)) {
                final org.apache.http.nio.reactor.ConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT, THREAD_FACTORY);
                final PoolingNHttpClientConnectionManager poolingmgr =
                    new PoolingNHttpClientConnectionManager(
                        ioreactor);
                httpAsyncClient = HttpAsyncClients.custom().setConnectionManager(poolingmgr).setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
                httpAsyncClient.start();
                return;
            } catch (Exception e) {
                throw new SpinException("构建SSL安全上下文失败", e);
            }
        }

        httpAsyncClient = org.apache.http.impl.nio.client.HttpAsyncClients.custom().setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
        httpAsyncClient.start();
    }
}
