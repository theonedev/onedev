package com.pmease.commons.util.match;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.StringUtils;

public class PatternSet {
	
	private final List<ExclusiveAwarePattern> patterns;
	
	public PatternSet(List<ExclusiveAwarePattern> patterns) {
		this.patterns = patterns;
	}
		
	public List<ExclusiveAwarePattern> getPatterns() {
		return patterns;
	}
	
	@Override
	public String toString() {
		List<String> list = new ArrayList<String>();
		for (ExclusiveAwarePattern each: getPatterns())
			list.add(each.toString());
		return StringUtils.join(list, ", ").trim();
	}
	
	public static PatternSet fromString(String str) {
		List<ExclusiveAwarePattern> patterns = new ArrayList<ExclusiveAwarePattern>();
		for (String pattern: StringUtils.splitAndTrim(str)) {
    		patterns.add(ExclusiveAwarePattern.fromString(pattern));
    	}
		return new PatternSet(patterns);
	}
	
}
