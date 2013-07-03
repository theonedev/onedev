package com.pmease.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
}
