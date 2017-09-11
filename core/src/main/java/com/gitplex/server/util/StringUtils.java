package com.gitplex.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Nullable;

import com.gitplex.jsyntax.TokenUtils;
import com.google.common.base.CaseFormat;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	public static String trimStart(String str) {
		return StringUtils.stripStart(str, " \r\n\t");
	}

	public static String trimEnd(String str) {
		return StringUtils.stripEnd(str, " \r\n\t");
	}
	
	/**
	 * Remove UTF BOM marker which might exists in some source code. UTF standard does not allow this marker and we 
	 * should remove it before doing something such as parsing source with ANTLR
	 */
	public static String removeBOM(String str) {
		return str.replace("\uFEFF", "");		
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
        return collection.toArray(new String[collection.size()]);
    }

    /**
     * Convert camel case string into lower case with underscore, for example:
     * input: FirstSecondThird
     * output: first_second_third
     * 
     * @param input
     * @return lower case string with underscore
     */
    public static @Nullable String camelCaseToLowerCaseWithUnderscore(@Nullable String input) {
    	if (input == null)
    		return input;
    	
    	return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, input);
    }
    
    /**
     * Convert camel case string into lower case with hyphen, for example:
     * input: FirstSecondThird
     * output: first-second-third
     * 
     * @param input
     * @return lower case string with hyphen
     */
    public static @Nullable String camelCaseToLowerCaseWithHyphen(@Nullable String input) {
    	if (input == null)
    		return input;
    	
    	return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, input);
    }
    
    public static String camelCaseToLowerCaseWithConnector(String input, char connector) {
    	if (input == null) return input; // garbage in, garbage out
        int length = input.length();
        StringBuilder result = new StringBuilder(length * 2);
        int resultLength = 0;
        boolean wasPrevTranslated = false;
        for (int i = 0; i < length; i++)
        {
            char c = input.charAt(i);
            if (i > 0 || c != connector) // skip first starting underscore
            {
                if (Character.isUpperCase(c))
                {
                    if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != connector)
                    {
                        result.append(connector);
                        resultLength++;
                    }
                    c = Character.toLowerCase(c);
                    wasPrevTranslated = true;
                }
                else
                {
                    wasPrevTranslated = false;
                }
                result.append(c);
                resultLength++;
            }
        }
        return resultLength > 0 ? result.toString() : input;
    }
    
    private static final String SINGLE_QUOTE = "\'";
    private static final String DOUBLE_QUOTE = "\"";
    /**
     * Put quotes around the given String if necessary.
     * <p>
     * If the argument doesn't include spaces or quotes, return it as is. If it
     * contains double quotes, use single quotes - else surround the argument by
     * double quotes.
     * </p>
     *
     * @param argument the argument to be quoted
     * @return the quoted argument
     * @throws IllegalArgumentException If argument contains both types of quotes
     */
    public static String quoteArgument(final String argument) {

        String cleanedArgument = argument.trim();

        // strip the quotes from both ends
        while(cleanedArgument.startsWith(SINGLE_QUOTE) || cleanedArgument.startsWith(DOUBLE_QUOTE)) {
            cleanedArgument = cleanedArgument.substring(1);
        }
        
        while(cleanedArgument.endsWith(SINGLE_QUOTE) || cleanedArgument.endsWith(DOUBLE_QUOTE)) {
            cleanedArgument = cleanedArgument.substring(0, cleanedArgument.length() - 1);
        }

        final StringBuffer buf = new StringBuffer();
        if (cleanedArgument.indexOf(DOUBLE_QUOTE) > -1) {
            if (cleanedArgument.indexOf(SINGLE_QUOTE) > -1) {
                throw new IllegalArgumentException(
                        "Can't handle single and double quotes in same argument");
            } else {
                return buf.append(SINGLE_QUOTE).append(cleanedArgument).append(
                        SINGLE_QUOTE).toString();
            }
        } else if (cleanedArgument.indexOf(SINGLE_QUOTE) > -1
                || cleanedArgument.indexOf(" ") > -1) {
            return buf.append(DOUBLE_QUOTE).append(cleanedArgument).append(
                    DOUBLE_QUOTE).toString();
        } else {
            return cleanedArgument;
        }
    }
    
    public static int calcLevenshteinDistance(String str1, String str2) {                          
        int len0 = str1.length() + 1;                                                     
        int len1 = str2.length() + 1;                                                     
     
        // the array of distances                                                       
        int[] cost = new int[len0];                                                     
        int[] newcost = new int[len0];                                                  
     
        // initial cost of skipping prefix in String s0                                 
        for (int i = 0; i < len0; i++) cost[i] = i;                                     
     
        // dynamically computing the array of distances                                  
     
        // transformation cost for each letter in s1                                    
        for (int j = 1; j < len1; j++) {                                                
            // initial cost of skipping prefix in String s1                             
            newcost[0] = j;                                                             
     
            // transformation cost for each letter in s0                                
            for(int i = 1; i < len0; i++) {                                             
                // matching current letters in both strings                             
                int match = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;             
     
                // computing cost for each transformation                               
                int cost_replace = cost[i - 1] + match;                                 
                int cost_insert  = cost[i] + 1;                                         
                int cost_delete  = newcost[i - 1] + 1;                                  
     
                // keep minimum cost                                                    
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }                                                                           
     
            // swap cost/newcost arrays                                                 
            int[] swap = cost; cost = newcost; newcost = swap;                          
        }                                                                               
     
        // the distance is the cost for transforming all letters in both strings        
        return cost[len0 - 1];                                                          
    }

    public static String escapeHtml(String text) {
    	return TokenUtils.escapeHtml(text);
    }

}
