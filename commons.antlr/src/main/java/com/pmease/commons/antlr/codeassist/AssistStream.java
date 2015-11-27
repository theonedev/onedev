package com.pmease.commons.antlr.codeassist;

import java.util.List;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;

public class AssistStream {
	
	private final List<Token> tokens;
	
	private int index;
	
	public AssistStream(List<Token> tokens) {
		Preconditions.checkArgument(!tokens.isEmpty() && tokens.get(tokens.size()-1).getType() == Token.EOF);
		this.tokens = tokens;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		Preconditions.checkArgument(index>=0 && index<size());
		this.index = index;
	}
	
	public void increaseIndex() {
		Preconditions.checkState(!isEof());
		index++;
	}

	public int indexOf(Token token) {
		return tokens.indexOf(token);
	}

	public int size() {
		return tokens.size();
	}
	
	public Token getCurrentToken() {
		return getToken(index);
	}
	
	public Token getNextToken() {
		return getToken(index+1);
	}
	
	public Token getToken(int index) {
		Preconditions.checkArgument(index>=0 && index<size());
		return tokens.get(index);
	}
	
	public int getTokenIndex(Token token) {
		return tokens.indexOf(token);
	}
	
	public Token getPreviousToken() {
		return getToken(index-1);
	}

	public boolean isEof() {
		return index == size()-1;
	}
	
}
