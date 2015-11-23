package com.pmease.commons.antlr;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

public class AntlrUtils {

	public static String getDefaultGrammarFile(Class<?> lexerClass) {
		String lexerName = lexerClass.getName().replace(".", "/");
		return lexerName.substring(0, lexerName.length() - "Lexer".length()) + ".g4";
	}
	
	public static String getDefaultTokenFile(Class<?> lexerClass) {
		return lexerClass.getSimpleName() + ".tokens";
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Lexer> getLexerClass(Class<? extends Parser> parserClass) {
		String parserClassName = parserClass.getName();
		String lexerClassName = parserClassName.substring(0, parserClassName.length()-"Parser".length()) + "Lexer";
		try {
			return (Class<? extends Lexer>) Class.forName(lexerClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
}
