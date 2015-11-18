package com.pmease.commons.antlr.grammarspec;

public class LexerRuleRefElementSpec extends TokenElementSpec {

	private final String ruleName;
	
	public LexerRuleRefElementSpec(String label, Multiplicity multiplicity, int tokenType, String ruleName) {
		super(label, multiplicity, tokenType);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
