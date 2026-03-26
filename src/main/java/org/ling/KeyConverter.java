package org.ling;

public interface KeyConverter<U> {
    U fromString(String s);
    String toString(U u);
}
