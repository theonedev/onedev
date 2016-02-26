package com.pmease.commons.util.match;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.util.Range;

public class WildcardUtils {
	
	private static final PatternMatcher pathMatcher = new WildcardPathMatcher();
	
	private static final PatternMatcher stringMatcher = new WildcardStringMatcher();
	
	/**
     * Tests whether or not a given path matches a given pattern using of 
     * <a href="http://ant.apache.org/manual/dirtasks.html">Ant path pattern</a>.
     * 
     * @param pattern
     * 			the pattern to match against. Must not be null
     * @param str     
     * 			the path to match, as a String. Must not be null
     *
     * @return 
     * 			true if the pattern matches the string, or false otherwise
     * 
     */
    public static boolean matchPath(String pattern, String path) {
    	return pathMatcher.matches(pattern, path);
    }
    
    /**
     * Tests whether or not a string matches specified pattern, which may contain 
     * two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern 
     * 			the pattern to match against. Must not be null.
     * @param str     
     * 			the string which must be matched against the pattern. Must not be null.
     * @return 
     * 			true if the string matches against the pattern, or false otherwise.
     */
    public static boolean matchString(String pattern, String str) {
    	return stringMatcher.matches(pattern, str);
    }
    
    public static boolean hasWildcards(String input) {
    	return (input.indexOf('*') != -1 || input.indexOf('?') != -1);
    }
    
    /**
     * Check the unit test for explanation.
     * 
     * @param text
     * @param wildcard
     * @param caseSensitive
     * @return
     */
    @Nullable
    public static WildcardApplied applyWildcard(String text, String wildcard, boolean caseSensitive) {
		String normalizedText;
		String normalizedWildcard;
		if (caseSensitive) {
			normalizedText = text;
			normalizedWildcard = wildcard;
		} else {
			normalizedText = text.toLowerCase();
			normalizedWildcard = wildcard.toLowerCase();
		}
		List<Range> literalRanges = new ArrayList<>();
		int pos = 0;
		int index = normalizedWildcard.indexOf('*');
		while (index != -1) {
			if (index>pos)
				literalRanges.add(new Range(pos, index));
			pos = index+1;
			index = normalizedWildcard.indexOf('*', pos);
		}
		if (normalizedWildcard.length()>pos)
			literalRanges.add(new Range(pos, normalizedWildcard.length()));

		String appliedText = wildcard;
		int first = -1;
		int last = 0;
		pos = 0;
		for (Range literalRange: literalRanges) {
			String literal = wildcard.substring(literalRange.getFrom(), literalRange.getTo());
			index = normalizedText.indexOf(literal, pos);
			if (index != -1) {
				if (first == -1)
					first = index;
				pos = index+literal.length();
				last = pos;
				appliedText = replaceLiteral(appliedText, literalRange, text.substring(index, pos));
			} else {
				return null;
			}
		}
		if (first != -1 && wildcard.charAt(0) != '*')
			appliedText = text.substring(0, first) + appliedText;
		else 
			first = 0;
		if (wildcard.length() == 0 || wildcard.charAt(wildcard.length()-1) != '*') {
			String suffix = text.substring(last);
			last = appliedText.length();
			appliedText = appliedText + suffix;
		} else {
			last = appliedText.length();
		}
		return new WildcardApplied(appliedText, new Range(first, last));
    }
	
	private static String replaceLiteral(String text, Range literalRange, String literal) {
		String prefix = text.substring(0, literalRange.getFrom());
		String suffix = text.substring(literalRange.getTo());
		return prefix + literal + suffix;
	}
	
}
