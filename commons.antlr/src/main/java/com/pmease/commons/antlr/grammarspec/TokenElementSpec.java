package com.pmease.commons.antlr.grammarspec;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

public abstract class TokenElementSpec extends ElementSpec {

	private final int type;

	public TokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, int type) {
		super(codeAssist, label, multiplicity);
		
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public Token newToken(final String text) {
		return new Token() {

			@Override
			public String getText() {
				return text;
			}

			@Override
			public int getType() {
				return type;
			}

			@Override
			public int getLine() {
				return -1;
			}

			@Override
			public int getCharPositionInLine() {
				return -1;
			}

			@Override
			public int getChannel() {
				return -1;
			}

			@Override
			public int getTokenIndex() {
				return -1;
			}

			@Override
			public int getStartIndex() {
				return -1;
			}

			@Override
			public int getStopIndex() {
				return -1;
			}

			@Override
			public TokenSource getTokenSource() {
				return null;
			}

			@Override
			public CharStream getInputStream() {
				return null;
			}
			
		};
	}
}
