package com.pmease.commons.lang.analyzer;

import com.pmease.commons.lang.tokenizer.Token;

public class LineAwareToken extends Token {

	private static final long serialVersionUID = 1L;

	private final int line;
	
	public LineAwareToken(Token token, int line) {
		super(token.style(), token.text());
		
		this.line = line;
	}

	public int getLine() {
		return line;
	}

}
