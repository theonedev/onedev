package com.pmease.commons.antlr;

public class RuleRefElement extends Element {

	private final String ruleName;
	
	public RuleRefElement(Multiplicity multiplicity, String ruleName) {
		super(multiplicity);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
