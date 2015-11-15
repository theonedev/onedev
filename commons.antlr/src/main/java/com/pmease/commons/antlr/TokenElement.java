package com.pmease.commons.antlr;

public class TokenElement extends Element {

	private final int tokenType;
	
	public TokenElement(String label, Multiplicity multiplicity, int tokenType) {
		super(label, multiplicity);
		
		this.tokenType = tokenType;
	}

	public int getTokenType() {
		return tokenType;
	}

}
