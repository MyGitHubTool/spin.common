package org.spin.core.util.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SpinException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * HTTP请求
 *
 * @param <T> 请求类型
 */
public final class Http<T extends HttpRequestBase> {

    private Function<URI, T> requestSuppiler;

    public static final Http<HttpGet> GET = new Http<>(HttpGet::new);
    public static final Http<HttpPost> POST = new Http<>(HttpPost::new);
    public static final Http<HttpDelete> DELETE = new Http<>(HttpDelete::new);
    public static final Http<HttpPut> PUT = new Http<>(HttpPut::new);
    public static final Http<HttpHead> HEAD = new Http<>(HttpHead::new);
    public static final Http<HttpOptions> OPTIONS = new Http<>(HttpOptions::new);
    public static final Http<HttpTrace> TRACE = new Http<>(HttpTrace::new);
    public static final Http<HttpPatch> PATCH = new Http<>(HttpPatch::new);

    private static final String SCHEMA = "http://";

    private Http(Function<URI, T> requestSuppiler) {
        this.requestSuppiler = requestSuppiler;
    }

    // region init and getter/setter

    public static HttpExecutor.HttpInitializer configure() {
        return HttpExecutor.configure();
    }

    public static int getSocketTimeout() {
        return HttpExecutor.getSocketTimeout();
    }

    public static int getConnectTimeout() {
        return HttpExecutor.getConnectTimeout();
    }

    public static int getMaxTotal() {
        return HttpExecutor.getDefaultMaxTotal();
    }

    public static int getMaxPerRoute() {
        return HttpExecutor.getDefaultMaxPerRoute();
    }

    public static HttpRequestRetryHandler getDefaultHttpRetryHandler() {
        return HttpExecutor.getDefaultHttpRetryHandler();
    }

    // endregion

    // region build

    /**
     * 从指定uri字符串构造请求
     *
     * @param uri uri地址
     * @return 请求对象
     */
    public final Request<T> withUrl(String uri) {
        URI u;
        try {
            URIBuilder uriBuilder = new URIBuilder(fixUrl(uri));
            u = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
        }
        return withUrl(u);
    }

    /**
     * 从指定uri对象构造请求
     *
     * @param uri uri地址
     * @return 请求对象
     */
    public final Request<T> withUrl(URI uri) {
        return new Request<>(requestSuppiler.apply(uri));
    }


    /**
     * 从指定uri字符串构造请求，并通过uriProc对uri对象进行自定义处理
     *
     * @param uri     uri地址
     * @param uriProc uri处理逻辑
     * @return 请求对象
     */
    public final Request<T> withUrl(String uri, FinalConsumer<URIBuilder> uriProc) {
        URI u;
        try {
            URIBuilder uriBuilder = new URIBuilder(fixUrl(uri));
            if (null != uriProc) {
                uriProc.accept(uriBuilder);
            }
            u = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
        }
        return withUrl(u);
    }

    // endregion

    private String fixUrl(String url) {
        return url.toLowerCase().startsWith("http") ? url : SCHEMA + url;
    }
}
