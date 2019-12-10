package org.spin.core.gson;

import org.apache.commons.lang.reflect.ConstructorUtils;
import org.spin.core.gson.adapter.*;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Gson TypeAdapterFactory
 * <p>Created by xuweinan on 2017/1/25.</p>
 *
 * @author xuweinan
 */
public class SpinTypeAdapterFactory implements TypeAdapterFactory {
    private String datePattern;
    private String localDatePatern;
    private String localTimePatern;
    private static List<MatchableTypeAdapter<?>> typeAdapters = new ArrayList<>();
    private boolean inited;
    private final Object lock = new Object();

    public SpinTypeAdapterFactory(String datePattern, String localDatePatern, String localTimePatern) {
        this.datePattern = datePattern;
        this.localDatePatern = localDatePatern;
        this.localTimePatern = localTimePatern;
    }

    @SuppressWarnings("unchecked")
    private void init(Gson gson) {
        if (!inited) {
            synchronized (lock) {
                if (!inited) {
                    inited = true;
                    try {
                        Class<MatchableTypeAdapter> cls = (Class<MatchableTypeAdapter>) ClassUtils.getClass("org.spin.data.gson.adapter.HibernateProxyTypeAdapter");
                        typeAdapters.add((MatchableTypeAdapter<?>) ConstructorUtils.invokeConstructor(cls, gson));
                        cls = (Class<MatchableTypeAdapter>) ClassUtils.getClass("org.spin.data.gson.adapter.HibernatePersistentBagTypeAdapter");
                        typeAdapters.add(cls.getConstructor().newInstance());
                    } catch (Exception ignore) {
                        // ignore
                    }
                    typeAdapters.add(new DateTypeAdapter(datePattern));
                    typeAdapters.add(new LocalDateTimeTypeAdapter(datePattern));
                    typeAdapters.add(new LocalDateTypeAdapter(localDatePatern));
                    typeAdapters.add(new LocalTimeTypeAdapter(localTimePatern));
                    typeAdapters.add(new LongTypeAdapter());
                    typeAdapters.add(new UserEnumTypeAdapter());
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (typeAdapters.isEmpty()) {
            init(gson);
        }
        return (TypeAdapter<T>) typeAdapters.stream().filter(t -> t.isMatch(type)).findFirst().orElse(null);
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
}
