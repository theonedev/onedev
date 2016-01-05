package com.pmease.commons.util.highlighter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pmease.commons.util.JoinedRanges;
import com.pmease.commons.util.Range;

public class Highlighter {

	public static String highlightRanges(String text, List<Range> ranges, 
			Transformer<String> matchedTransformer, Transformer<String> unmatchedTransformer) {
		StringBuffer buffer = new StringBuffer();
    	int start = 0;
    	for (Range range: new JoinedRanges(ranges)) {
    		buffer.append(unmatchedTransformer.transform(text.substring(start, range.getFrom())));
    		buffer.append(matchedTransformer.transform(text.substring(range.getFrom(), range.getTo())));
    		start = range.getTo();
    	}
    	buffer.append(unmatchedTransformer.transform(text.substring(start, text.length())));
    	return buffer.toString();
	}

	public static String highlightPatterns(String text, List<Pattern> patterns, 
			Transformer<String> matchedTransformer, Transformer<String> unmatchedTransformer) {
		List<Range> ranges = new ArrayList<>();
		for (Pattern pattern: patterns) {
	    	Matcher matcher = pattern.matcher(text);
	    	while (matcher.find()) 
	    		ranges.add(new Range(matcher.start(), matcher.end()));
		}
		return highlightRanges(text, ranges, matchedTransformer, unmatchedTransformer);
	}
	
	public static String highlightLiterals(String text, List<String> literals, boolean caseSensitive,
			Transformer<String> matchedTransformer, Transformer<String> unmatchedTransformer) {
		List<Range> ranges = new ArrayList<>();
		for (String literal: literals) {
			text.indexOf(literal);
	    	Matcher matcher = pattern.matcher(text);
	    	while (matcher.find()) 
	    		ranges.add(new Range(matcher.start(), matcher.end()));
		}
		return highlightRanges(text, ranges, matchedTransformer, unmatchedTransformer);
	}
	
}
