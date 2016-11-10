package com.gitplex.commons.lang.extractors;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractStream {

	private static final Logger logger = LoggerFactory.getLogger(ExtractStream.class);
	
	private final List<ExtractToken> tokens;
	
	private int pos = -1; // 0-indexed position of current token 
	
	public ExtractStream(List<ExtractToken> tokens) {
		this.tokens = tokens;
		this.tokens.add(ExtractToken.EOF);
	}
	
	public ExtractStream(Lexer lexer, TokenFilter filter) {
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				logger.error("Error lexing at position '" + line + ":" + charPositionInLine + "': " + msg);
			}
			
		});
		tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			if (filter.accept(token))
				tokens.add(new ExtractToken(token));
			token = lexer.nextToken();
		}
		tokens.add(ExtractToken.EOF);
	}
	
	public ExtractToken at(int pos) {
		if (pos >=0 && pos < tokens.size())
			return tokens.get(pos);
		else
			throw new ExtractException("Invalid token position: " + pos);
	}
	
	public ExtractToken next() {
		return at(++pos);
	}
	
	public ExtractToken lookAhead(int ahead) {
		return at(pos+ahead);
	}
	
	public ExtractToken lookBehind(int behind) {
		return at(pos-behind);
	}
	
	public ExtractToken nextType(int type) {
		ExtractToken token = next();
		while(true) {
			if (token.is(type))
				return token;
			token = next();
		}
	}
	
	public ExtractToken nextType(int...types) {
		ExtractToken token = next();
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
	public ExtractToken nextClosed(int openType, int closeType) {
		int nestingLevel = 1;
		ExtractToken balanced = nextType(openType, closeType);
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
	public List<ExtractToken> between(int startPos, int endPos) {
		List<ExtractToken> tokens = new ArrayList<>();
		for (int i=startPos; i<=endPos; i++) 
			tokens.add(at(i));
		return tokens;
	}
	
	public ExtractToken current() {
		return at(pos);
	}
	
	public void seek(int index) {
		this.pos = index;
	}
	
}
