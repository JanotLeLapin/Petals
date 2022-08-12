package io.github.petals;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import redis.clients.jedis.JedisPooled;

public class Metadata implements Map<String, String> {
    String path;
    JedisPooled pooled;

    public Metadata(String path, JedisPooled pooled) {
        this.path = path;
        this.pooled = pooled;
    }

    @Override
    public void clear() {
        this.pooled.del(path);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.pooled.hget(path, key.toString()) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.values().contains(value);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        // TODO: Implement this
        return null;
    }

    @Override
    public String get(Object key) {
        return this.pooled.hget(path, key.toString());
    }

    @Override
    public String getOrDefault(Object key, String defaultValue) {
        String value = this.get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public String merge(String key, String value, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        if (this.get(key) == null) this.put(key, value);
        return value;
    }

    @Override
    public Set<String> keySet() {
        return pooled.hkeys(path);
    }

    @Override
    public String put(String key, String value) {
        this.pooled.hset(path, key, value);
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> data) {
        data.forEach((k, v) -> this.put(k, v));
    }

    @Override
    public String putIfAbsent(String key, String value) {
        if (pooled.hget(path, key) == null) {
            this.put(key, value);
            return null;
        } else return value;
    }

    @Override
    public String remove(Object key) {
        this.pooled.hdel(path, key.toString());
        return null;
    }

    @Override
    public String replace(String key, String value) {
        if (this.get(key) != null) this.put(key, value);
        return value;
    }

    @Override
    public boolean replace(String key, String oldValue, String newValue) {
        if (this.get(key).equals(oldValue)) {
            this.put(key, newValue);
            return true;
        } else return false;
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        try {
            this
                .keySet()
                .forEach(k -> this.put(k, function.apply(k, this.get(k))));
        } catch (Exception e) {}
    }

    @Override
    public int size() {
        return (int) this.pooled.hlen(path);
    }

    @Override
    public Collection<String> values() {
        return this.pooled.hvals(path);
    }
}

