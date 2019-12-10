package org.spin.core.collection;

import java.util.Map;
import java.util.Set;

public interface MultiDiffValueMap<K, V> extends Map<K, Set<V>> {

    V getFirst(K key);

    void add(K key, V value);

    void set(K key, V value);

    void setAll(Map<K, V> values);

    Map<K, V> toSingleValueMap();
}
