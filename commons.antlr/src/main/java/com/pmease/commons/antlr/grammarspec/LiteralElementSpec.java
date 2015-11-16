package com.pmease.commons.antlr.grammarspec;

public class LiteralElementSpec extends TokenElementSpec {

	private static final long serialVersionUID = 1L;

	private final String literal;
	
	public LiteralElementSpec(Grammar grammar, String label, Multiplicity multiplicity, int tokenType, String literal) {
		super(grammar, label, multiplicity, tokenType);
		
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

}
