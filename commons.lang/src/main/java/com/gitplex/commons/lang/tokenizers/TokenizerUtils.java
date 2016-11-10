package com.gitplex.commons.lang.tokenizers;

/**
 * Utility class to simulate some javascript methods
 * @author robin
 *
 */
public class TokenizerUtils {

	private static int normalizeStrIndex(String str, int index) {
		if (index < 0) {
			index = str.length() + index;
			if (index < 0)
				index = 0;
		} else if (index > str.length()) {
			index = str.length();
		}
		return index;
	}
	
	public static String substr(String str, int start) {
		return str.substring(normalizeStrIndex(str, start));
	}
	
	public static String substr(String str, int start, int length) {
		start = normalizeStrIndex(str, start);
		if (length <= 0)
			return "";
		int end = start + length;
		if (end > str.length())
			return str.substring(start);
		else
			return str.substring(start, end);
	}

	public static String slice(String str, int start) {
		start = normalizeStrIndex(str, start);
		return str.substring(start);
	}
	
	public static String slice(String str, int start, int end) {
		start = normalizeStrIndex(str, start);
		end = normalizeStrIndex(str, end);
		if (start > end)
			return "";
		else
			return str.substring(start, end);
	}
	
}
