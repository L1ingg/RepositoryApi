package org.ling;

import java.util.UUID;

public class UuidKeyConverter implements KeyConverter<UUID> {
    public UUID fromString(String s) { return UUID.fromString(s); }
    public String toString(UUID u) { return u.toString(); }
}
