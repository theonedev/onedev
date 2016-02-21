package com.pmease.commons.util.match;

public interface RuleMatcher<T, V> {
	boolean matches(T rule, V value);
}
