package com.gitplex.commons.lang.extractors;

import org.antlr.v4.runtime.Token;

/**
 * This class represents a ANTLR token filter
 * 
 * @author robin
 *
 */
public interface TokenFilter {
	
	boolean accept(Token token);
	
	public static final TokenFilter ALL = new TokenFilter() {

		@Override
		public boolean accept(Token token) {
			return true;
		}
		
	};
	
	public static final TokenFilter DEFAULT_CHANNEL = new TokenFilter() {

		@Override
		public boolean accept(Token token) {
			return token.getChannel() == Token.DEFAULT_CHANNEL;
		}
		
	};
	
}