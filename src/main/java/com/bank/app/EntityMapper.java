package com.bank.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class EntityMapper<K, V> implements Comparable<EntityMapper<K, V>> {

    private List<K> keys = new ArrayList<>();
    private List<V> values = new ArrayList<>();

    public void put(K key, V value) {
        int i = keys.indexOf(key);
        if (i >= 0) {
            values.set(i, value);
        } else {
            keys.add(key);
            values.add(value);
        }
    }

    public V get(K key) {
        int i = keys.indexOf(key);
        return i < 0 ? null : values.get(i);
    }

    public boolean containsKey(K key) {
        return keys.indexOf(key) >= 0;
    }

    public V remove(K key) {
        int i = keys.indexOf(key);
        if (i < 0) {
            return null;
        }
        keys.remove(i);
        return values.remove(i);
    }

    public int size() {
        return keys.size();
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public List<K> getKeys() {
        return new ArrayList<>(keys);
    }

    public void setKeys(List<K> keys) {
        if (keys == null || keys.size() != values.size()) {
            throw new IllegalArgumentException("Число ключей должно совпадать с числом значений");
        }
        this.keys = new ArrayList<>(keys);
    }

    public List<V> getValues() {
        return new ArrayList<>(values);
    }

    public void setValues(List<V> values) {
        if (values == null || values.size() != keys.size()) {
            throw new IllegalArgumentException("Число значений должно совпадать с числом ключей");
        }
        this.values = new ArrayList<>(values);
    }

    @Override
    public int compareTo(EntityMapper<K, V> other) {
        return Integer.compare(this.size(), other.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityMapper<?, ?> m)) return false;
        return keys.equals(m.keys) && values.equals(m.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys, values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EntityMapper{");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(keys.get(i)).append("=").append(values.get(i));
        }
        return sb.append("}").toString();
    }
}