package com.pmease.commons.antlr.grammarspec;

public class RuleRefElementSpec extends ElementSpec {

	private final String ruleName;
	
	public RuleRefElementSpec(String label, Multiplicity multiplicity, String ruleName) {
		super(label, multiplicity);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
