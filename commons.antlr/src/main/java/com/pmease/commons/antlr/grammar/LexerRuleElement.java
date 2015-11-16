package com.pmease.commons.antlr.grammar;

public class LexerRuleElement extends TokenElement {

	private static final long serialVersionUID = 1L;

	private final String ruleName;
	
	public LexerRuleElement(Grammar grammar, String label, Multiplicity multiplicity, int tokenType, String ruleName) {
		super(grammar, label, multiplicity, tokenType);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
