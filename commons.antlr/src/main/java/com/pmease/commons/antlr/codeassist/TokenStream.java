package com.pmease.commons.antlr.codeassist;

import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

public class TokenStream {
	
	private final List<Token> tokens;
	
	private int index;
	
	public TokenStream(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int indexOf(Token token) {
		return tokens.indexOf(token);
	}

	public Token getToken(int index) {
		return tokens.get(index);
	}
	
	@Nullable
	public Token nextToken() {
		if (index < tokens.size()) {
			return tokens.get(index++);
		} else {
			return null;
		}
	}
	
	public int size() {
		return tokens.size();
	}
	
	public boolean isEmpty() {
		return tokens.isEmpty();
	}
	
	public Token getFirstToken() {
		return tokens.get(0);
	}
	
	public Token getLastToken() {
		return tokens.get(tokens.size()-1);
	}
	
}
