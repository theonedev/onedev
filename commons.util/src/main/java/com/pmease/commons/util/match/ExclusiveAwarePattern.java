package com.pmease.commons.util.match;

public class ExclusiveAwarePattern {
	
	private final String pattern;
	
	private final boolean exclusive;
	
	public ExclusiveAwarePattern(String pattern, boolean exclusive) {
		this.pattern = pattern;
		this.exclusive = exclusive;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	@Override
	public String toString() {
		if (exclusive)
			return "-" + pattern;
		else
			return pattern;
	}

	public static ExclusiveAwarePattern fromString(String str) {
		str = str.trim();

		boolean exclusive = false;
		if (str.startsWith("+"))
			str = str.substring(1).trim();
		if (str.startsWith("-")) {
			str = str.substring(1).trim();
			exclusive = true;
		}
		
		return new ExclusiveAwarePattern(str, exclusive);
	}
	
}