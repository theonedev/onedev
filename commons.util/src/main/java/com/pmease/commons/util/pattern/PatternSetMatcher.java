package com.pmease.commons.util.pattern;

import java.util.List;

public class PatternSetMatcher implements PatternMatcher {

	private final PatternMatcher patternMatcher;
	
	public PatternSetMatcher(PatternMatcher patternMatcher) {
		this.patternMatcher = patternMatcher;
	}
	
	@Override
	public boolean matches(String patterns, String input) {
		boolean lastExclusive = false;
		for (ExclusiveAwarePattern each: PatternSet.fromString(patterns).getPatterns()) {
			if (each.isExclusive()) {
				lastExclusive = true;
				if (patternMatcher.matches(each.getPattern(), input))
					return false;
			} else {
				lastExclusive = false;
				if (patternMatcher.matches(each.getPattern(), input))
					return true;
			}
		}
    	return lastExclusive;
	}

	public boolean matches(List<String> includes, List<String> excludes, String input) {
		for (String exclude: excludes) {
			if (patternMatcher.matches(exclude, input))
				return false;
		}
		if (includes.isEmpty()) {
			return true;
		} else {
			for (String include: includes) {
				if (patternMatcher.matches(include, input))
					return true;
			}
			return false;
		}
	}
	
}
