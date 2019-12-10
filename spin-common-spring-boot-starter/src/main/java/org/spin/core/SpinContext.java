package org.spin.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文缓存及全局变量
 * <p>集中管理缓存与常量，可以集中清空，以应对热加载后的不一致问题</p>
 * Created by xuweinan on 2016/9/5.
 *
 * @author xuweinan
 */
public final class SpinContext {
    public static final Map<String, Map<String, Field>> BEAN_FIELDS = new ConcurrentHashMap<>();

    /** 线程绑定的全局公用属性 */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_PARAMETERS = ThreadLocal.withInitial(HashMap::new);

    private SpinContext() {
    }

    /**
     * 注册与当前前程绑定的参数，如果参数已存在，则覆盖
     *
     * @param key 参数名
     * @param param 参数值
     */
    public static void putLocalParam(String key, Object param) {
        THREAD_LOCAL_PARAMETERS.get().put(key, param);
    }

    /**
     * 获取与当前线程绑定的参数
     *
     * @param key 参数名
     * @return 参数值，不存在则为null
     */
    public static Object getLocalParam(String key) {
        return SpinContext.THREAD_LOCAL_PARAMETERS.get().get(key);
    }

    /**
     * 移除与当前线程绑定的参数
     *
     * @param key 参数名
     * @return 被移除的参数值(不存在则为null)
     */
    public static Object removeLocalParam(String key) {
        return SpinContext.THREAD_LOCAL_PARAMETERS.get().remove(key);
    }

    public synchronized static void clearCache() {
        BEAN_FIELDS.clear();
    }
}
