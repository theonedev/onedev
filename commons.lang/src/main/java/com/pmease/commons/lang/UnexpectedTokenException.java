package com.pmease.commons.lang;

@SuppressWarnings("serial")
public class UnexpectedTokenException extends ExtractException {

	public UnexpectedTokenException(LangToken token) {
		super("Unexpected token: " + token);
	}

}
