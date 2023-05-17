package io.onedev.server.util.match;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.LinearRange;

public class WildcardUtils {
	
	private static final PathMatcher PATH_MATCHER = new PathMatcher();
	
	private static final StringMatcher STRING_MATCHER = new StringMatcher();
	
	/**
     * Tests whether or not a given path matches a given pattern using of 
     * <a href="http://ant.apache.org/manual/dirtasks.html">Ant path pattern</a>.
     * 
     * @param pattern
     * 			the pattern to match against. Must not be null
     * @param path     
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
     * @param string     
     * 			the string which must be matched against the pattern. Must not be null.
     * @return 
     * 			true if the string matches against the pattern, or false otherwise.
     */
    public static boolean matchString(String pattern, String string) {
    	return STRING_MATCHER.matches(pattern, string);
    }
    
    private static int indexOf(String string, String substring, int index) {
        if (index >= string.length()) {
            return (substring.length() == 0 ? string.length(): -1);
        }
        if (index < 0) {
        	index = 0;
        }
        if (substring.length() == 0) {
            return index;
        }

        char first = substring.charAt(0);
        int max = (string.length() - substring.length());

        for (int i = index; i <= max; i++) {
            /* Look for first character. */
            if ('?' != first && string.charAt(i) != first) {
                while (++i <= max && '?' != first && string.charAt(i) != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + substring.length() - 1;
                for (int k = 1; j < end && ('?' == substring.charAt(k) || string.charAt(j) == substring.charAt(k)); j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }
        return -1;
    }
    
    public static List<LinearRange> getLiteralRanges(String pattern) {
		List<LinearRange> literalRanges = new ArrayList<>();
		int pos = 0;
		int index = pattern.indexOf('*');
		while (index != -1) {
			if (index>pos)
				literalRanges.add(new LinearRange(pos, index));
			pos = index+1;
			index = pattern.indexOf('*', pos);
		}
		if (pattern.length() > pos)
			literalRanges.add(new LinearRange(pos, pattern.length()));
    	return literalRanges;
    }
    
    /**
     * Check the unit test for explanation.
     * 
     * @param string
     * @param pattern
     * @param caseSensitive
     * @return
     */
    @Nullable
    public static PatternApplied applyStringPattern(String pattern, String string, boolean caseSensitive) {
		String normalizedText;
		String normalizedPattern;
		if (caseSensitive) {
			normalizedText = string;
			normalizedPattern = pattern;
		} else {
			normalizedText = string.toLowerCase();
			normalizedPattern = pattern.toLowerCase();
		}
		List<LinearRange> literalRanges = getLiteralRanges(normalizedPattern);
		
		StringBuilder appliedText = new StringBuilder(pattern);
		int first = -1;
		int last = 0;
		int pos = 0;
		for (LinearRange literalRange: literalRanges) {
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
						appliedText.append(string.charAt(index+i));
				}
				appliedText.append(temp.substring(literalRange.getTo()));				
			} else {
				return null;
			}
		}
		if (first != -1 && pattern.charAt(0) != '*')
			appliedText = new StringBuilder(string.substring(0, first) + appliedText.toString());
		else 
			first = 0;
		if (pattern.length() == 0 || pattern.charAt(pattern.length()-1) != '*') {
			String suffix = string.substring(last);
			last = appliedText.length();
			appliedText.append(suffix);
		} else {
			last = appliedText.length();
		}
		return new PatternApplied(appliedText.toString(), new LinearRange(first, last));
    }
    
    @Nullable
    public static PatternApplied applyPathPattern(String pattern, String path, boolean caseSensitive) {
    	String normalizedPattern = pattern;
    	String normalizedPath = path;
    	if (!caseSensitive) {
    		normalizedPattern = pattern.toLowerCase();
    		normalizedPath = path.toLowerCase();
    	}
		Iterable<String> literals = Splitter.on(new CharMatcher() {

			@Override
			public boolean matches(char c) {
				return c=='/' || c=='?' || c=='*';
			}
			
		}).split(normalizedPattern);

		// fast pass
		for (String literal: literals) {
			if (!normalizedPath.contains(literal))
				return null;
		}

    	if (pattern.equals("**") && path.startsWith("/"))
    		return null;
    	else if (pattern.length() == 0)
    		return new PatternApplied(path, new LinearRange(0, 0));
    	
    	List<String> patternSegments = Splitter.on("/").splitToList(pattern);
    	List<String> pathSegments = Splitter.on("/").splitToList(path);

    	if (patternSegments.iterator().next().equals("**")) {
    		return applyPathPattern(patternSegments, pathSegments, true, caseSensitive);
    	} else {
        	for (int i=0; i<pathSegments.size(); i++) {
        		PatternApplied patternApplied = applyPathPattern(patternSegments, 
        				pathSegments.subList(i, pathSegments.size()), true, caseSensitive);
        		if (patternApplied != null) {
        			if (i == 0) {
        				return patternApplied;
        			} else {
    					String prefix = Joiner.on("/").join(pathSegments.subList(0, i));
    					LinearRange match = patternApplied.getMatch();
    					LinearRange newMatch = new LinearRange(
    							match.getFrom() + prefix.length() + 1, 
    							match.getTo() + prefix.length() + 1);
    					return new PatternApplied(prefix + "/" + patternApplied.getText(), newMatch);
        			}
        		}
        	}
        	
        	return null;
    	}
    }
    
    @Nullable
    private static PatternApplied applyPathPattern(List<String> patternSegments, List<String> pathSegments, 
    		boolean firstSegment, boolean caseSensitive) {
    	String patternSegment = patternSegments.get(0);
    	if (patternSegment.equals("**")) {
    		if (patternSegments.size() == 1)
    			return new PatternApplied("**", new LinearRange(0, 2));
    		for (int pathIndex = 0; pathIndex < pathSegments.size(); pathIndex++) {
    			PatternApplied patternApplied = applyPathPattern(
    					patternSegments.subList(1, patternSegments.size()), 
    					pathSegments.subList(pathIndex, pathSegments.size()), 
    					false, caseSensitive);
    			if (patternApplied != null) {
    				LinearRange newMatch = new LinearRange(0, patternApplied.getMatch().getTo()+3);
    				return new PatternApplied("**/" + patternApplied.getText(), newMatch);
    			}
    		}
    		return null;
    	} else if (pathSegments.isEmpty()) {
    		return null;
    	} else {
    		String pathSegment = pathSegments.get(0);
    		PatternApplied patternApplied;
    		if (firstSegment && patternSegments.size() != 1) {
        		patternApplied = applyStringPattern(
        				StringUtils.reverse(patternSegment), StringUtils.reverse(pathSegment), caseSensitive);
        		if (patternApplied != null) {
	        		String text = patternApplied.getText();
	        		LinearRange match = patternApplied.getMatch();
	        		patternApplied = new PatternApplied(
	        				StringUtils.reverse(text), 
	        				new LinearRange(text.length()-match.getTo(), text.length()-match.getFrom()));
        		}
    		} else {
        		patternApplied = applyStringPattern(patternSegment, pathSegment, caseSensitive);
    		}
    		if (patternApplied != null) {
    			LinearRange match = patternApplied.getMatch();
    			if (!firstSegment && match.getFrom() != 0 
    					|| patternSegments.size() != 1 && match.getTo() != patternApplied.getText().length()) {
    				return null;
    			}
    			if (patternSegments.size() == 1) {
    				if (pathSegments.size() == 1)
    					return patternApplied;
    				else {
    					String suffix = Joiner.on("/").join(pathSegments.subList(1, pathSegments.size()));
    					return new PatternApplied(
    							patternApplied.getText() + "/" + suffix, 
    							patternApplied.getMatch());
    				}
    			} else {
	    			PatternApplied leftOverPatternApplied = applyPathPattern(
	    					patternSegments.subList(1, patternSegments.size()), 
	    					pathSegments.subList(1, pathSegments.size()), 
	    					false, 
	    					caseSensitive);
	    			if (leftOverPatternApplied != null) {
	    				LinearRange leftOverRange = leftOverPatternApplied.getMatch();
	    				LinearRange newRange = new LinearRange(
	    						patternApplied.getMatch().getFrom(), 
	    						leftOverRange.getTo() + patternApplied.getText().length() + 1);
	    				return new PatternApplied(
	    						patternApplied.getText() + "/" + leftOverPatternApplied.getText(),
	    						newRange);
	    			}
    			}
    		} 
    		return null;
    	}
    }
    
    @Nullable
    public static LinearRange rangeOfMatch(String pattern, String string) {
		List<LinearRange> literalRanges = getLiteralRanges(pattern);

		int first = -1;
		int pos = 0;
		for (LinearRange literalRange: literalRanges) {
			String literal = pattern.substring(literalRange.getFrom(), literalRange.getTo());
			int index = indexOf(string, literal, pos);
			if (index != -1) {
				if (first == -1)
					first = index;
				pos = index+literal.length();
			} else {
				return null;
			}
		}
		return new LinearRange(first, pos);
    }

}
