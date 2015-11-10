package com.pmease.commons.wicket.behavior.inputassist.antlr;

import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import com.pmease.commons.wicket.behavior.inputassist.InputError;

public class CollectErrorsParserErrorListener extends BaseErrorListener {

	private final List<InputError> errors;
	
	public CollectErrorsParserErrorListener(List<InputError> errors) {
		this.errors = errors;
	}
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		CommonToken token = (CommonToken) offendingSymbol;
		int end;
		if (token != null && token.getType() != Token.EOF)
			end = charPositionInLine + token.getText().length();
		else
			end = charPositionInLine + 1;
		errors.add(new InputError(charPositionInLine, end));
	}

}
