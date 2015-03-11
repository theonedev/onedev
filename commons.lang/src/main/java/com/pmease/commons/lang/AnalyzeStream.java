package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

public class AnalyzeStream {

	private final List<AnalyzeToken> tokens;
	
	private int pos = -1; // 0-indexed position of current token 
	
	public AnalyzeStream(List<AnalyzeToken> tokens) {
		this.tokens = tokens;
		this.tokens.add(AnalyzeToken.EOF);
	}
	
	public AnalyzeStream(Lexer lexer, TokenFilter filter) {
		tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			if (filter.accept(token))
				tokens.add(new AnalyzeToken(token));
			token = lexer.nextToken();
		}
		tokens.add(AnalyzeToken.EOF);
	}
	
	public AnalyzeToken at(int pos) {
		if (pos >=0 && pos < tokens.size())
			return tokens.get(pos);
		else
			throw new AnalyzeException("Invalid token position: " + pos);
	}
	
	public AnalyzeToken next() {
		return at(++pos);
	}
	
	public AnalyzeToken lookAhead(int ahead) {
		return at(pos+ahead);
	}
	
	public AnalyzeToken lookBehind(int behind) {
		return at(pos-behind);
	}
	
	public AnalyzeToken nextType(int...anyTypes) {
		AnalyzeToken token = next();
		while(true) {
			for (int type: anyTypes) {
				if (token.is(type))
					return token;
			}
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
	public AnalyzeToken nextClosed(int openType, int closeType) {
		int nestingLevel = 1;
		AnalyzeToken balanced = nextType(openType, closeType);
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
	public List<AnalyzeToken> between(int startPos, int endPos) {
		List<AnalyzeToken> tokens = new ArrayList<>();
		for (int i=startPos; i<=endPos; i++) 
			tokens.add(at(i));
		return tokens;
	}

	public AnalyzeToken current() {
		return at(pos);
	}
	
	public void seek(int index) {
		this.pos = index;
	}
	
}
