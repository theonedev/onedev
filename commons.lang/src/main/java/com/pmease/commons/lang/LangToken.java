package com.pmease.commons.lang;

import java.io.Serializable;

import org.antlr.v4.runtime.Token;

public class LangToken implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final LangToken EOF = new LangToken(Token.EOF, "", -1, -1);
	
	private final int type;
	
	private final String text;
	
	private final int line;
	
	private final int posInLine;
	
	public LangToken(Token antlrToken) {
		this.type = antlrToken.getType();
		this.text = antlrToken.getText();
		this.line = antlrToken.getLine()-1;
		this.posInLine = antlrToken.getCharPositionInLine();
	}

	public LangToken(int type, String text, int line, int posInLine) {
		this.type = type;
		this.text = text;
		this.line = line;
		this.posInLine = posInLine;
	}
	
	public boolean is(int type) {
		return this.type == type; 
	}
	
	public boolean is(int...types) {
		for (int type: types) {
			if (this.type == type)
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
	
	public int getPosInLine() {
		return posInLine;
	}

	public LangToken checkType(int... expectedTypes) {
		if (!is(expectedTypes))
			throw new UnexpectedTokenException(this);
		return this;
	}
	
	public LangToken checkText(String expectedText) {
		if (!text.equals(expectedText))
			throw new UnexpectedTokenException(this);
		return this;
	}
	
	@Override
	public String toString() {
		return text;
	}

}
