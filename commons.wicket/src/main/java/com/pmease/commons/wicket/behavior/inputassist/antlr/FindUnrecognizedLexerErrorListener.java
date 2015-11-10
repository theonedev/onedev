package com.pmease.commons.wicket.behavior.inputassist.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class FindUnrecognizedLexerErrorListener extends BaseErrorListener {
	
	private final String input;
	
	private String unrecognized;
	
	public FindUnrecognizedLexerErrorListener(String input) {
		this.input = input;
	}
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
			int charPositionInLine, String msg, RecognitionException e) {
		if (input.length() == recognizer.getInputStream().index()+1
				|| input.length() == recognizer.getInputStream().index()) {
			unrecognized = input.substring(charPositionInLine);
		}
	}

	public String getUnrecognized() {
		return unrecognized;
	}

}
