package com.pmease.commons.antlr.grammarabstraction;

public class LexerRuleElement extends TokenElement {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	public LexerRuleElement(Grammar grammar, String label, Multiplicity multiplicity, int tokenType, String name) {
		super(grammar, label, multiplicity, tokenType);
		
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
