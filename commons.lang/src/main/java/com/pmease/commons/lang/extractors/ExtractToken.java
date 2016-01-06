package com.pmease.commons.lang.extractors;

import java.io.Serializable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.util.Range;

public class ExtractToken implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final ExtractToken EOF = new ExtractToken(Token.EOF, "", null);
	
	private final int type;
	
	private final String text;
	
	private final TokenPosition pos;
	
	public ExtractToken(Token antlrToken) {
		type = antlrToken.getType();
		text = antlrToken.getText();
		Range range = new Range(antlrToken.getCharPositionInLine(), 
				antlrToken.getCharPositionInLine() + text.length());
		pos = new TokenPosition(antlrToken.getLine()-1, range);
	}

	public ExtractToken(int type, String text, TokenPosition pos) {
		this.type = type;
		this.text = text;
		this.pos = pos;
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

	public TokenPosition getPos() {
		return pos;
	}
	
	public ExtractToken checkType(int... expectedTypes) {
		if (!is(expectedTypes))
			throw new UnexpectedTokenException(this);
		return this;
	}
	
	public ExtractToken checkText(String expectedText) {
		if (!text.equals(expectedText))
			throw new UnexpectedTokenException(this);
		return this;
	}
	
	@Override
	public String toString() {
		return text;
	}

}
