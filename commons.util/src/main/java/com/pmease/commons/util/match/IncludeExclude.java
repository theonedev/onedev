package com.pmease.commons.util.match;

import java.util.List;

public class IncludeExclude<T, V> {

	private final List<T> includes;
	
	private final List<T> excludes;
	
	public IncludeExclude(List<T> includes, List<T> excludes) {
		this.includes = includes;
		this.excludes = excludes;
	}
	
	public boolean matches(Matcher<T, V> matcher, V value) {
		for (T exclude: excludes) {
			if (matcher.matches(exclude, value))
				return false;
		}
		if (includes.isEmpty()) {
			return true;
		} else {
			for (T include: includes) {
				if (matcher.matches(include, value))
					return true;
			}
			return false;
		}
	}
	
}
