package com.pmease.commons.lang;

import org.antlr.v4.runtime.Token;

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