package com.pmease.commons.util.pattern;

public class PatternSetMatcher implements PatternMatcher {

	private final PatternMatcher patternMatcher;
	
	public PatternSetMatcher(PatternMatcher patternMatcher) {
		this.patternMatcher = patternMatcher;
	}
	
	@Override
	public boolean matches(String patterns, String input) {
		for (ExclusiveAwarePattern each: PatternSet.fromString(patterns).getPatterns()) {
			if (each.isExclusive()) {
				if (patternMatcher.matches(each.getPattern(), input))
					return false;
			} else {
				if (patternMatcher.matches(each.getPattern(), input))
					return true;
			}
		}
    	return false;
	}

}
