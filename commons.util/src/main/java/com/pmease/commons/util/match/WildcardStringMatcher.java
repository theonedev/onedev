package com.pmease.commons.util.match;

import org.apache.tools.ant.types.selectors.SelectorUtils;

public class WildcardStringMatcher implements PatternMatcher {
	
	@Override
	public boolean matches(String pattern, String input) {
		return SelectorUtils.match(pattern, input);		
	}

}
