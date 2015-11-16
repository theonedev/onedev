package com.pmease.commons.antlr.grammar;

public class TokenElement extends Element {

	private static final long serialVersionUID = 1L;

	private final int tokenType;

	public TokenElement(Grammar grammar, String label, Multiplicity multiplicity, int tokenType) {
		super(grammar, label, multiplicity);
		
		this.tokenType = tokenType;
	}

	public int getTokenType() {
		return tokenType;
	}

}
