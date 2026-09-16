package io.onedev.server.persistence.dao;

import java.io.Serializable;

public record Order(String property, boolean ascending) implements Serializable {
    public static Order asc(String property) { return new Order(property, true); }
    public static Order desc(String property) { return new Order(property, false); }
}
