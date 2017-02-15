package com.gitplex.server.util.match;

public interface Matcher<R, V> {
	boolean matches(R rule, V value);
}
