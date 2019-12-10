package org.spin.core.collection;

import java.io.Serializable;
import java.util.*;

public class HashMultiValueMap<K, V> implements MultiValueMap<K, V>, Serializable {

    private static final long serialVersionUID = 3801124242820219131L;

    private final Map<K, List<V>> targetMap;

    public HashMultiValueMap() {
        this.targetMap = new HashMap<>();
    }

    public HashMultiValueMap(int initialCapacity) {
        this.targetMap = new HashMap<>(initialCapacity);
    }

    public HashMultiValueMap(Map<K, List<V>> otherMap) {
        this.targetMap = new HashMap<>(otherMap);
    }

    @Override
    public void add(K key, V value) {
        List<V> values = this.targetMap.computeIfAbsent(key, k -> new ArrayList<>());
        values.add(value);
    }

    @Override
    public V getFirst(K key) {
        List<V> values = this.targetMap.get(key);
        return (values != null ? values.get(0) : null);
    }

    @Override
    public void set(K key, V value) {
        List<V> values = new ArrayList<>();
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
        for (Entry<K, List<V>> entry : this.targetMap.entrySet()) {
            singleValueMap.put(entry.getKey(), entry.getValue().get(0));
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
    public List<V> get(Object key) {
        return this.targetMap.get(key);
    }

    @Override
    public List<V> put(K key, List<V> value) {
        return this.targetMap.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return this.targetMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> map) {
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
    public Collection<List<V>> values() {
        return this.targetMap.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return this.targetMap.entrySet();
    }

    public HashMultiValueMap<K, V> deepCopy() {
        HashMultiValueMap<K, V> copy = new HashMultiValueMap<>(this.targetMap.size());
        for (Map.Entry<K, List<V>> entry : this.targetMap.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }


    @Override
    public boolean equals(Object obj) {
        return this.targetMap.equals(obj);
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
