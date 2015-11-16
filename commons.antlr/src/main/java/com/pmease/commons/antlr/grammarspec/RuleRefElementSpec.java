package com.pmease.commons.antlr.grammarspec;

public class RuleRefElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final String ruleName;
	
	public RuleRefElementSpec(Grammar grammar, String label, Multiplicity multiplicity, String ruleName) {
		super(grammar, label, multiplicity);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
