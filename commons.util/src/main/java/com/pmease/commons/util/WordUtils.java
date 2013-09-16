package com.pmease.commons.util;

public class WordUtils extends org.apache.commons.lang3.text.WordUtils {

	public static String uncamel(String name) {
		StringBuffer b = new StringBuffer();
		char[] chars = name.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isUpperCase(c) && (nextCharacterIsNotUpperCase(chars, i) || 
					previousCharacterIsNotUpperCase(chars, i))) {
				b.append(" ");
			} else if (Character.isDigit(c) && previousCharacterIsNotDigit(chars, i)) {
				b.append(" ");
			}
			b.append(c);
		}
		String result = b.toString();
		return result.trim();
	}

	public static boolean previousCharacterIsNotDigit(char[] chars, int i) {
		boolean hasPreviousCharacter = (i < 1);
		return hasPreviousCharacter || !Character.isDigit(chars[i - 1]);
	}

	private static boolean previousCharacterIsNotUpperCase(char[] chars, int i) {
		boolean hasPreviousCharacter = (i < 1);
		return hasPreviousCharacter || !Character.isUpperCase(chars[i - 1]);
	}

	public static boolean nextCharacterIsNotUpperCase(char[] chars, int i) {
		boolean hasNextCharacter = (i + 1 < chars.length);
		return hasNextCharacter && !Character.isUpperCase(chars[i + 1]);
	}
}
