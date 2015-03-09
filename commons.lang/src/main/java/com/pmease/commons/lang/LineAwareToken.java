package com.pmease.commons.lang;

public class LineAwareToken extends Token {

	private static final long serialVersionUID = 1L;

	private final int linePos;
	
	public LineAwareToken(String style, String text, int linePos) {
		super(style, text);
		this.linePos = linePos;
	}

	public LineAwareToken(Token token, int linePos) {
		this(token.style(), token.text(), linePos);
	}

	public int getLinePos() {
		return linePos;
	}
	
}
