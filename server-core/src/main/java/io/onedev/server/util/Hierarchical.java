package io.onedev.server.util;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

public interface Hierarchical extends Serializable {

    @Nullable
    Hierarchical getParent();

    @Nullable
    <T> T getData(Class<T> clazz);

}
