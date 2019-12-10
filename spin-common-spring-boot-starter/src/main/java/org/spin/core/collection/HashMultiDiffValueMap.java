package org.spin.core.collection;

import java.io.Serializable;
import java.util.*;

public class HashMultiDiffValueMap<K, V> implements MultiDiffValueMap<K, V>, Serializable {

    private static final long serialVersionUID = 3801124242820219131L;

    private final Map<K, Set<V>> targetMap;


    public HashMultiDiffValueMap() {
        this.targetMap = new HashMap<>();
    }

    public HashMultiDiffValueMap(int initialCapacity) {
        this.targetMap = new HashMap<>(initialCapacity);
    }

    public HashMultiDiffValueMap(Map<K, Set<V>> otherMap) {
        this.targetMap = new HashMap<>(otherMap);
    }

    @Override
    public void add(K key, V value) {
        Set<V> values = this.targetMap.computeIfAbsent(key, k -> new HashSet<>());
        values.add(value);
    }

    @Override
    public V getFirst(K key) {
        Set<V> values = this.targetMap.get(key);
        return (values != null && values.size() > 0 ? values.iterator().next() : null);
    }

    @Override
    public void set(K key, V value) {
        Set<V> values = new HashSet<>();
        values.add(value);
        this.targetMap.put(key, values);
    }

    @Override
    public void setAll(Map<K, V> values) {
        for (Entry<K, V> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<K, V> toSingleValueMap() {
        HashMap<K, V> singleValueMap = new HashMap<>(this.targetMap.size());
        for (Entry<K, Set<V>> entry : this.targetMap.entrySet()) {
            Set<V> values = entry.getValue();
            singleValueMap.put(entry.getKey(), (values != null && values.size() > 0 ? values.iterator().next() : null));
        }
        return singleValueMap;
    }


    // Map implementation

    @Override
    public int size() {
        return this.targetMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.targetMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.targetMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.targetMap.containsValue(value);
    }

    @Override
    public Set<V> get(Object key) {
        return this.targetMap.get(key);
    }

    @Override
    public Set<V> put(K key, Set<V> value) {
        return this.targetMap.put(key, value);
    }

    @Override
    public Set<V> remove(Object key) {
        return this.targetMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Set<V>> map) {
        this.targetMap.putAll(map);
    }

    @Override
    public void clear() {
        this.targetMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.targetMap.keySet();
    }

    @Override
    public Collection<Set<V>> values() {
        return this.targetMap.values();
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        return this.targetMap.entrySet();
    }

    public HashMultiDiffValueMap<K, V> deepCopy() {
        HashMultiDiffValueMap<K, V> copy = new HashMultiDiffValueMap<>(this.targetMap.size());
        for (Entry<K, Set<V>> entry : this.targetMap.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }


    @Override
    public boolean equals(Object obj) {
        return null != obj && this.getClass().equals(obj.getClass()) && this.targetMap.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.targetMap.hashCode();
    }

    @Override
    public String toString() {
        return this.targetMap.toString();
    }

}
