package com.gitplex.server.util;

import java.io.Serializable;

public interface Provider<T> extends Serializable {
    T get();
}