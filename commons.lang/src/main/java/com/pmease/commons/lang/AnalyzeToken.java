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
	
	public boolean is(String text) {
		return text.equals(this.text);
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
	
	public AnalyzeToken checkType(int... expectedTypes) {
		if (!is(expectedTypes))
			throw new UnexpectedTokenException(this);
		return this;
	}
	
	public AnalyzeToken checkText(String expectedText) {
		if (!text.equals(expectedText))
			throw new UnexpectedTokenException(this);
		return this;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper("Token")
				.add("type", type)
				.add("text", text)
				.add("line", line)
				.toString();
	}

}
