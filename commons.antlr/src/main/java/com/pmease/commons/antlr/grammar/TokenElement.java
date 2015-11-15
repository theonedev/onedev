package com.pmease.commons.antlr.grammar;

public class TokenElement extends Element {

	private static final long serialVersionUID = 1L;

	private final int tokenType;

	private final String tokenName;
	
	public TokenElement(Grammar grammar, String label, Multiplicity multiplicity, int tokenType, String tokenName) {
		super(grammar, label, multiplicity);
		
		this.tokenType = tokenType;
		this.tokenName = tokenName;
	}

	public int getTokenType() {
		return tokenType;
	}

	public String getTokenName() {
		return tokenName;
	}

}
