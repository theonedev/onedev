package com.pmease.commons.antlr.grammarabstraction;

public class LiteralElement extends TokenElement {

	private static final long serialVersionUID = 1L;

	private final String literal;
	
	public LiteralElement(Grammar grammar, String label, Multiplicity multiplicity, int tokenType, String literal) {
		super(grammar, label, multiplicity, tokenType);
		
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

}
