package com.pmease.commons.lang;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Objects;

public class AnalyzeToken {

	public static final AnalyzeToken EOF = new AnalyzeToken(Token.EOF, "", -1);
	
	private final int type;
	
	private final String text;
	
	private final int line;
	
	public AnalyzeToken(Token antlrToken) {
		this.type = antlrToken.getType();
		this.text = antlrToken.getText();
		this.line = antlrToken.getLine();
	}

	public AnalyzeToken(int type, String text, int line) {
		this.type = type;
		this.text = text;
		this.line = line;
	}
	
	public boolean is(int...tokenTypes) {
		for (int tokenType: tokenTypes) {
			if (type == tokenType)
				return true;
		}
		return false;
	}
	
	public boolean isEof() {
		return type == Token.EOF;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public int getLine() {
		return line;
	}

	public void expectType(int... expectedTypes) {
		for (int expectedType: expectedTypes) {
			if (expectedType == type)
				return;
		}
		
		throw new UnexpectedTokenException(this);
	}
	
	public void expectText(String expectedText) {
		if (!text.equals(expectedText))
			throw new UnexpectedTokenException(this);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper("Token").add("type", type).add("text", text).add("line", line).toString();
	}
	
}
