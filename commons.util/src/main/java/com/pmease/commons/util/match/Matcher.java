package com.pmease.commons.util.match;

public interface Matcher<R, V> {
	boolean matches(R rule, V value);
}
