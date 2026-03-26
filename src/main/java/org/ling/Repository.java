package org.ling;

import java.util.Map;
import java.util.Optional;

public interface Repository<U, T> {
    Map<U, T> getAll();
    Optional<T> get(U key);
    void add(U key, T value);
    void remove(U key);
    boolean hasKey(U key);
    boolean hasValue(T value);
}
