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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Nullable
	public Token nextToken() {
		if (index < tokens.size())
			return tokens.get(index);
		else
			return null;
	}
	
	public int getSize() {
		return tokens.size();
	}
	
}
