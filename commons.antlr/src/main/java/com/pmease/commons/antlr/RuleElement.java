package com.pmease.commons.antlr;

public class RuleElement extends Element {

	private final int ruleId;
	
	public RuleElement(String label, Multiplicity multiplicity, int ruleId) {
		super(label, multiplicity);
		
		this.ruleId = ruleId;
	}

	public int getRuleId() {
		return ruleId;
	}

}
