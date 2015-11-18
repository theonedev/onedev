package com.pmease.commons.antlr.grammarspec;

public class LiteralElementSpec extends TokenElementSpec {

	private final String literal;
	
	public LiteralElementSpec(String label, Multiplicity multiplicity, int tokenType, String literal) {
		super(label, multiplicity, tokenType);
		
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

}
