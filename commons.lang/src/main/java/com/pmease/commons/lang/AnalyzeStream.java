package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

public class AnalyzeStream {

	private final List<AnalyzeToken> tokens;
	
	private int tokenPos = 0; // 0-indexed position of current token 
	
	public AnalyzeStream(List<AnalyzeToken> tokens) {
		this.tokens = tokens;
	}
	
	public AnalyzeStream(Lexer lexer, TokenFilter filter) {
		tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			if (filter.accept(token))
				tokens.add(new AnalyzeToken(token));
			token = lexer.nextToken();
		}
	}
	
	public AnalyzeToken at(int tokenPos) {
		if (tokenPos >=0 && tokenPos < tokens.size())
			return tokens.get(tokenPos);
		else
			return AnalyzeToken.EOF;
	}
	
	public AnalyzeToken next() {
		return nextCount(1);
	}
	
	public AnalyzeToken nextCount(int count) {
		tokenPos += count;
		return at(tokenPos);
	}
	
	public AnalyzeToken lookAhead(int ahead) {
		return at(tokenPos+ahead);
	}
	
	public AnalyzeToken lookBehind(int behind) {
		return at(tokenPos-behind);
	}
	
	public AnalyzeToken nextType(int...anyTypes) {
		AnalyzeToken token = next();
		while(!token.isEof()) {
			for (int type: anyTypes) {
				if (token.is(type))
					return token;
			}
			token = next();
		}
		return token;
	}

	public AnalyzeToken nextClosed(int openType, int closeType) {
		int nestingLevel = 1;
		AnalyzeToken balanced = nextType(openType, closeType);
		while (!balanced.isEof()) {
			if (balanced.is(closeType)) {
				if (--nestingLevel == 0)
					return balanced;
			} else if (balanced.is(openType)) {
				nestingLevel++;
			}
			balanced = nextType(openType, closeType);
		}
		return balanced;
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
	public List<AnalyzeToken> between(int startPos, int endPos) {
		List<AnalyzeToken> tokens = new ArrayList<>();
		for (int i=startPos; i<=endPos; i++) 
			tokens.add(at(i));
		return tokens;
	}

	public AnalyzeToken current() {
		return nextCount(0);
	}
	
}
