package com.pmease.commons.util.match;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.util.Range;

public class WildcardUtils {
	
	private static final PatternMatcher PATH_MATCHER = new WildcardPathMatcher();
	
	private static final PatternMatcher STRING_MATCHER = new WildcardStringMatcher();
	
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
    	return PATH_MATCHER.matches(pattern, path);
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
    	return STRING_MATCHER.matches(pattern, str);
    }
    
    public static boolean hasWildcards(String str) {
    	return (str.indexOf('*') != -1 || str.indexOf('?') != -1);
    }
    
    private static int indexOf(String str, String substr, int index) {
        if (index >= str.length()) {
            return (substr.length() == 0 ? str.length(): -1);
        }
        if (index < 0) {
        	index = 0;
        }
        if (substr.length() == 0) {
            return index;
        }

        char first = substr.charAt(0);
        int max = (str.length() - substr.length());

        for (int i = index; i <= max; i++) {
            /* Look for first character. */
            if ('?' != first && str.charAt(i) != first) {
                while (++i <= max && '?' != first && str.charAt(i) != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + substr.length() - 1;
                for (int k = 1; j < end && ('?' == substr.charAt(k) || str.charAt(j) == substr.charAt(k)); j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }
        return -1;
    }
    
    private static List<Range> getLiteralRanges(String pattern) {
		List<Range> literalRanges = new ArrayList<>();
		int pos = 0;
		int index = pattern.indexOf('*');
		while (index != -1) {
			if (index>pos)
				literalRanges.add(new Range(pos, index));
			pos = index+1;
			index = pattern.indexOf('*', pos);
		}
		if (pattern.length()>pos)
			literalRanges.add(new Range(pos, pattern.length()));
    	return literalRanges;
    }
    
    /**
     * Check the unit test for explanation.
     * 
     * @param str
     * @param pattern
     * @param caseSensitive
     * @return
     */
    @Nullable
    public static PatternApplied applyPattern(String pattern, String str, boolean caseSensitive) {
		String normalizedText;
		String normalizedPattern;
		if (caseSensitive) {
			normalizedText = str;
			normalizedPattern = pattern;
		} else {
			normalizedText = str.toLowerCase();
			normalizedPattern = pattern.toLowerCase();
		}
		List<Range> literalRanges = getLiteralRanges(normalizedPattern);

		StringBuilder appliedText = new StringBuilder(pattern);
		int first = -1;
		int last = 0;
		int pos = 0;
		for (Range literalRange: literalRanges) {
			String literal = normalizedPattern.substring(literalRange.getFrom(), literalRange.getTo());
			int index = indexOf(normalizedText, literal, pos);
			if (index != -1) {
				if (first == -1)
					first = index;
				pos = index+literal.length();
				last = pos;

				String temp = appliedText.toString();
				appliedText = new StringBuilder(appliedText.substring(0, literalRange.getFrom()));
				for (int i=0; i<literal.length(); i++) {
					if (literal.charAt(i) == '?')
						appliedText.append('?');
					else
						appliedText.append(str.charAt(index+i));
				}
				appliedText.append(temp.substring(literalRange.getTo()));				
			} else {
				return null;
			}
		}
		if (first != -1 && pattern.charAt(0) != '*')
			appliedText = new StringBuilder(str.substring(0, first) + appliedText.toString());
		else 
			first = 0;
		if (pattern.length() == 0 || pattern.charAt(pattern.length()-1) != '*') {
			String suffix = str.substring(last);
			last = appliedText.length();
			appliedText.append(suffix);
		} else {
			last = appliedText.length();
		}
		return new PatternApplied(appliedText.toString(), new Range(first, last));
    }

    @Nullable
    public static Range rangeOfMatch(String pattern, String str) {
		List<Range> literalRanges = getLiteralRanges(pattern);

		int first = -1;
		int pos = 0;
		for (Range literalRange: literalRanges) {
			String literal = pattern.substring(literalRange.getFrom(), literalRange.getTo());
			int index = indexOf(str, literal, pos);
			if (index != -1) {
				if (first == -1)
					first = index;
				pos = index+literal.length();
			} else {
				return null;
			}
		}
		return new Range(first, pos);
    }

}
