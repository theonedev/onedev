package com.pmease.commons.antlr.grammar;

public class RuleElement extends Element {

	private static final long serialVersionUID = 1L;
	
	private final String ruleName;
	
	public RuleElement(Grammar grammar, String label, Multiplicity multiplicity, String ruleName) {
		super(grammar, label, multiplicity);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
