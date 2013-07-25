package com.pmease.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.types.selectors.SelectorUtils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * Split specified string with specified separator and trim the result fields. 
     * @param str 
     * @param separator
     * @return 
     * 			Modifiable collection of splitted fields. Leading and trailing white spaces will be trimmed 
     * 			from these fields. Element of the resulting collection will never be null or 
     * 			empty string.   
     */
	public static Collection<String> splitAndTrim(String str, String separator) {
		Collection<String> fields = new ArrayList<String>();
		for (String each: StringUtils.split(str, separator)) {
			if (each != null && each.trim().length() != 0)
				fields.add(each.trim());
		}
		return fields;
	}

	/**
	 * Parse specified string into tokens. Content surrounded with &quot; character
	 * is considered as a single token. For example: echo "hello world" will be parsed 
	 * into two tokens, respectively [echo], and [hello world]. The quote character 
	 * itself can be quoted and escaped in order to return as ordinary character. For 
	 * example: echo "hello \" world" will be parsed into two tokens: [echo] and 
	 * [hello " world].
	 * @param string
	 * @return
	 */
    public static String[] parseQuoteTokens(String string) {
    	List<String> commandTokens = new ArrayList<String>();
    	
		StreamTokenizer st = new StreamTokenizer(
				new BufferedReader(new StringReader(string)));
		st.resetSyntax();
		st.wordChars(0, 255);
		st.ordinaryChar(' ');
		st.ordinaryChar('\n');
		st.ordinaryChar('\t');
		st.ordinaryChar('\r');
		st.quoteChar('"');
		
		try {
			String token = null;
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == '"' || st.ttype == StreamTokenizer.TT_WORD) {
					if (token == null)
						token = st.sval;
					else
						token += st.sval;
				} else if (token != null) {
					commandTokens.add(token);
					token = null;
				}
			}
			if (token != null)
				commandTokens.add(token);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return commandTokens.toArray(new String[commandTokens.size()]);
    }	
    
    /**
     * Tests whether or not a string matches against a pattern.
     * The pattern may contain two special characters:<br>
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
    public static boolean wildcardMatch(String pattern, String str) {
    	return SelectorUtils.match(pattern, str);
    }
    
    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     * Trims tokens and omits empty tokens.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using <code>delimitedListToStringArray</code>
     * <p/>
     * <p>Copied from the Spring Framework while retaining all license, copyright and author information.
     *
     * @param str        the String to tokenize
     * @param delimiters the delimiter characters, assembled as String
     *                   (each of those characters is individually considered as delimiter).
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see java.lang.String#trim()
     */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using <code>delimitedListToStringArray</code>
     * <p/>
     * <p>Copied from the Spring Framework while retaining all license, copyright and author information.
     *
     * @param str               the String to tokenize
     * @param delimiters        the delimiter characters, assembled as String
     *                          (each of those characters is individually considered as delimiter)
     * @param trimTokens        trim the tokens via String's <code>trim</code>
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens (<code>null</code> if the input String
     *         was <code>null</code>)
     * @see java.util.StringTokenizer
     * @see java.lang.String#trim()
     */
    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    /**
     * Copy the given Collection into a String array.
     * The Collection must contain String elements only.
     * <p/>
     * <p>Copied from the Spring Framework while retaining all license, copyright and author information.
     *
     * @param collection the Collection to copy
     * @return the String array (<code>null</code> if the passed-in
     *         Collection was <code>null</code>)
     */
    public static String[] toStringArray(Collection<?> collection) {
        if (collection == null) {
            return null;
        }
        return (String[]) collection.toArray(new String[collection.size()]);
    }
}
