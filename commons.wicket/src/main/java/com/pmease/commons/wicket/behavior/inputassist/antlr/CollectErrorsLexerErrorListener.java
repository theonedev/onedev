package com.pmease.commons.wicket.behavior.inputassist.antlr;

import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.pmease.commons.wicket.behavior.inputassist.InputError;

public class CollectErrorsLexerErrorListener extends BaseErrorListener {

	private final List<InputError> errors;
	
	public CollectErrorsLexerErrorListener(List<InputError> errors) {
		this.errors = errors;
	}
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		errors.add(new InputError(charPositionInLine, recognizer.getInputStream().index()+1));
	}

}
