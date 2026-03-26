package org.ling;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InMemoryRepository<U, T> {

    private final Map<U, T> map = new ConcurrentHashMap<>();

    public Map<U, T> getAll() {
        return Map.copyOf(this.map);
    }

    public Optional<T> get(U key) {
        return Optional.ofNullable(this.map.get(key));
    }

    public void add(U key, T value) {
        this.map.put(key, value);
    }

    public boolean hasKey(U key) {
        return this.map.containsKey(key);
    }

    public boolean hasValue(T value) {
        return this.map.containsValue(value);
    }

    public void remove(U key) {
        this.map.remove(key);
    }
}
