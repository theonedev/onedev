package com.gitplex.commons.lang.extractors;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenStream {

	private static final Logger logger = LoggerFactory.getLogger(TokenStream.class);
	
	private final List<Token> tokens;
	
	private int pos = -1; // 0-indexed position of current token 
	
	public TokenStream(List<Token> tokens) {
		this.tokens = tokens;
		this.tokens.add(Token.EOF);
	}
	
	public TokenStream(Lexer lexer, TokenFilter filter) {
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				logger.error("Error lexing at position '" + line + ":" + charPositionInLine + "': " + msg);
			}
			
		});
		tokens = new ArrayList<>();
		org.antlr.v4.runtime.Token token = lexer.nextToken();
		while (token.getType() != org.antlr.v4.runtime.Token.EOF) {
			if (filter.accept(token))
				tokens.add(new Token(token));
			token = lexer.nextToken();
		}
		tokens.add(Token.EOF);
	}
	
	public Token at(int pos) {
		if (pos >=0 && pos < tokens.size())
			return tokens.get(pos);
		else
			throw new ExtractException("Invalid token position: " + pos);
	}
	
	public Token next() {
		return at(++pos);
	}
	
	public Token lookAhead(int ahead) {
		return at(pos+ahead);
	}
	
	public Token lookBehind(int behind) {
		return at(pos-behind);
	}
	
	public Token nextType(int type) {
		Token token = next();
		while(true) {
			if (token.is(type))
				return token;
			token = next();
		}
	}
	
	public Token nextType(int...types) {
		Token token = next();
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
	public Token nextClosed(int openType, int closeType) {
		int nestingLevel = 1;
		Token balanced = nextType(openType, closeType);
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
	public List<Token> between(int startPos, int endPos) {
		List<Token> tokens = new ArrayList<>();
		for (int i=startPos; i<=endPos; i++) 
			tokens.add(at(i));
		return tokens;
	}
	
	public Token current() {
		return at(pos);
	}
	
	public void seek(int index) {
		this.pos = index;
	}
	
}
