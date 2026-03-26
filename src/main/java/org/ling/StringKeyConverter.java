package org.ling;

public class StringKeyConverter implements KeyConverter<String> {
    public String fromString(String s) { return s; }
    public String toString(String u) { return u; }
}
