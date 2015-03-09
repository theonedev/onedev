package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.List;

public class TokenStream {

	private static final LineAwareToken EOF = new LineAwareToken("", "", -1); 
			
	private final List<LineAwareToken> tokens;
	
	private int tokenPos = 0; // 0-indexed position of current token 
	
	public TokenStream(List<LineAwareToken> tokens) {
		this.tokens = tokens;
	}
	
	public LineAwareToken getToken(int tokenPos) {
		if (tokenPos >=0 && tokenPos < tokens.size())
			return tokens.get(tokenPos);
		else
			return EOF;
	}
	
	public LineAwareToken next() {
		return nextCount(1);
	}
	
	public LineAwareToken nextCount(int count) {
		tokenPos += count;
		return getToken(tokenPos);
	}
	
	public LineAwareToken lookAhead(int ahead) {
		return getToken(tokenPos+ahead);
	}
	
	public LineAwareToken lookBehind(int behind) {
		return getToken(tokenPos-behind);
	}
	
	public LineAwareToken nextSymbol(String...anySymbols) {
		LineAwareToken token = next();
		while(!token.isEof()) {
			for (String text: anySymbols) {
				if (token.is(text))
					return token;
			}
			token = next();
		}
		return token;
	}

	private LineAwareToken nextBalanced(String open, String close) {
		int nestingLevel = 1;
		LineAwareToken balanced = nextSymbol(open, close);
		while (!balanced.isEof()) {
			if (balanced.is(close)) {
				if (--nestingLevel == 0)
					return balanced;
			} else if (balanced.is(open)) {
				nestingLevel++;
			}
			balanced = nextSymbol(open, close);
		}
		return balanced;
	}
	
	public LineAwareToken nextBalanced(Token token) {
		if (token.is("{")) {
			return nextBalanced("{", "}");
		} else if (token.is("[")) {
			return nextBalanced("[", "]");
		} else if (token.is("<")) {
			return nextBalanced("<", ">");
		} else if (token.is("(")) {
			return nextBalanced("(", ")");
		} else {
			throw new IllegalStateException("Not a balanceable token: " + token.text());
		}
	}
	
	public int tokenPos() {
		return tokenPos;
	}

	/**
	 * Get tokens between startPos and endPos
	 * 
	 * @param startPos 
	 * 			0-indexed start position, inclusive  
	 * @param endPos 
	 * 			0-indexed end position, inclusive
	 * @return 
	 * 			list of tokens between startPos and endPos 
	 */
	public List<LineAwareToken> tokens(int startPos, int endPos) {
		List<LineAwareToken> tokens = new ArrayList<>();
		for (int i=startPos; i<=endPos; i++) 
			tokens.add(getToken(i));
		return tokens;
	}

	public LineAwareToken current() {
		return nextCount(0);
	}
	
}
