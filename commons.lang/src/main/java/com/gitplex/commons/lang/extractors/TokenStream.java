package com.gitplex.commons.lang.extractors;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Token stream holds a list of tokens parsed from source file, and provides various 
 * methods to work with these tokens
 * 
 * @author robin
 *
 */
public class TokenStream {

	private static final Logger logger = LoggerFactory.getLogger(TokenStream.class);
	
	private final List<Token> tokens;
	
	private int index = -1; // index of current token in stream 
	
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
	
	/**
	 * Get token at specified index in stream
	 * 
	 * @param index
	 * 			index of the token in stream
	 * @return
	 * 			token at specified index
	 * @throws
	 * 			ExtractException if index is invalid
	 */
	public Token at(int index) {
		if (index >=0 && index < tokens.size())
			return tokens.get(index);
		else
			throw new ExtractException("Invalid token position: " + index);
	}
	
	/**
	 * Get next token in stream and move index forward
	 * 
	 * @return
	 * 			next token in stream
	 * @throws 
	 * 			ExtractException if no token at next index
	 */
	public Token next() {
		return at(++index);
	}
	
	/**
	 * Get next N token in stream without moving index
	 * 
	 * @param ahead
	 * 			number of tokens to look ahead
	 * @return
	 * 			next N token in stream
	 * @throws
	 * 			ExtractException if no token at specified look ahead index
	 */
	public Token lookAhead(int ahead) {
		return at(index+ahead);
	}
	
	/**
	 * Get previous N token in stream without moving index
	 * 
	 * @param behind
	 * 			number of tokens to look behind
	 * @return
	 * 			previous N token in stream
	 * @throws
	 * 			ExtractException if no token at specified look behind index
	 */
	public Token lookBehind(int behind) {
		return at(index-behind);
	}
	
	/**
	 * Get next token of specified type and move index forward
	 * 
	 * @param type
	 * 			ANTLR lexer token type
	 * @return
	 * 			next token of specified type
	 * @throws 
	 * 			ExtractException if no subsequent token matches specified type 
	 */
	public Token nextType(int type) {
		Token token = next();
		while(true) {
			if (token.is(type))
				return token;
			token = next();
		}
	}
	
	/**
	 * Get next token of specified types and move index forward
	 * 
	 * @param types
	 * 			ANTLR lexer token types
	 * @return
	 * 			next token of specified types
	 * @throws 
	 * 			ExtractException if no subsequent token matches specified types
	 */
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
	 * 			open type to seek 
	 * @param closeType
	 * 			close type to seek
	 * @return
	 * 			token at stop position
	 * @throws 
	 * 			ExtractException if no stop token can be found 
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
	
	/**
	 * Get current index in stream
	 * 
	 * @return
	 * 			current index in stream
	 */
	public int index() {
		return index;
	}

	/**
	 * Set current index in stream
	 * 
	 * @param index
	 * 			index to set
	 */
	public void index(int index) {
		this.index = index;
	}
	
	/**
	 * Get tokens between a range
	 * 
	 * @param startIndex 
	 * 			0-indexed start index, inclusive  
	 * @param endIndex 
	 * 			0-indexed end index, inclusive
	 * @return 
	 * 			list of tokens between specified range 
	 * @throws 
	 * 			ExtractException if no token at specified range 
	 */
	public List<Token> between(int startIndex, int endIndex) {
		List<Token> tokens = new ArrayList<>();
		for (int i=startIndex; i<=endIndex; i++) 
			tokens.add(at(i));
		return tokens;
	}
	
	/**
	 * Get token at current index
	 * 
	 * @return
	 * 			token at current index
	 */
	public Token current() {
		return at(index);
	}
	
	/**
	 * Seek to specified index
	 * 
	 * @param index
	 * 			index to seek to
	 */
	public void seek(int index) {
		this.index = index;
	}
	
}
