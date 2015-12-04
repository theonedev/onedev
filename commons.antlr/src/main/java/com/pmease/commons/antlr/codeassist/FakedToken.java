package com.pmease.commons.antlr.codeassist;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

public class FakedToken implements Token {

	private final int index;
	
	public FakedToken(AssistStream stream) {
		this.index = stream.getIndex()-1;
	}
	
	public FakedToken(int index) {
		this.index = index;
	}
	
	@Override
	public String getText() {
		return null;
	}

	@Override
	public int getType() {
		return -1;
	}

	@Override
	public int getLine() {
		return -1;
	}

	@Override
	public int getCharPositionInLine() {
		return -1;
	}

	@Override
	public int getChannel() {
		return 0;
	}

	@Override
	public int getTokenIndex() {
		return index;
	}

	@Override
	public int getStartIndex() {
		return -1;
	}

	@Override
	public int getStopIndex() {
		return -1;
	}

	@Override
	public TokenSource getTokenSource() {
		return null;
	}

	@Override
	public CharStream getInputStream() {
		return null;
	}

}
