package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

public class LangStream {

	private final List<LangToken> tokens;
	
	private int pos = -1; // 0-indexed position of current token 
	
	public LangStream(List<LangToken> tokens) {
		this.tokens = tokens;
		this.tokens.add(LangToken.EOF);
	}
	
	public LangStream(Lexer lexer, TokenFilter filter) {
		tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			if (filter.accept(token))
				tokens.add(new LangToken(token));
			token = lexer.nextToken();
		}
		tokens.add(LangToken.EOF);
	}
	
	public LangToken at(int pos) {
		if (pos >=0 && pos < tokens.size())
			return tokens.get(pos);
		else
			throw new AnalyzeException("Invalid token position: " + pos);
	}
	
	public LangToken next() {
		return at(++pos);
	}
	
	public LangToken lookAhead(int ahead) {
		return at(pos+ahead);
	}
	
	public LangToken lookBehind(int behind) {
		return at(pos-behind);
	}
	
	public LangToken nextType(int type) {
		LangToken token = next();
		while(true) {
			if (token.is(type))
				return token;
			token = next();
		}
	}
	
	public LangToken nextType(int...types) {
		LangToken token = next();
		while(true) {
			if (token.is(types))
				return token;
			token = next();
		}
	}

	/**
	 * Seek to next closed type, with balancing open/close considered, that is, all open/close pair 
	 * of the same type will be skipped. Below is an example of start position and stop position 
	 * when calling this method.
	 *  
	 * text { text { text }  } 
	 *      ^                ^
	 *      seek here        stop here
	 *                         
	 * @param openType
	 * @param closeType
	 * @return
	 */
	public LangToken nextClosed(int openType, int closeType) {
		int nestingLevel = 1;
		LangToken balanced = nextType(openType, closeType);
		while (true) {
			if (balanced.is(closeType)) {
				if (--nestingLevel == 0)
					return balanced;
			} else if (balanced.is(openType)) {
				nestingLevel++;
			}
			balanced = nextType(openType, closeType);
		}
	}
	
	public int pos() {
		return pos;
	}

	public void pos(int pos) {
		this.pos = pos;
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
	public List<LangToken> between(int startPos, int endPos) {
		List<LangToken> tokens = new ArrayList<>();
		for (int i=startPos; i<=endPos; i++) 
			tokens.add(at(i));
		return tokens;
	}
	
	public List<LangToken> allType(int...types) {
		List<LangToken> typeTokens = new ArrayList<>();
		for (LangToken token: tokens) {
			if (token.is(types))
				typeTokens.add(token);
		}
		return typeTokens;
	}
	
	public List<LangToken> allType(int type) {
		List<LangToken> typeTokens = new ArrayList<>();
		for (LangToken token: tokens) {
			if (token.is(type))
				typeTokens.add(token);
		}
		return typeTokens;
	}
	
	public LangToken current() {
		return at(pos);
	}
	
	public void seek(int index) {
		this.pos = index;
	}
	
}
