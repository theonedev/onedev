package com.pmease.commons.antlr.grammarspec;

public class LexerRuleRefElementSpec extends TokenElementSpec {

	private static final long serialVersionUID = 1L;

	private final String ruleName;
	
	public LexerRuleRefElementSpec(Grammar grammar, String label, Multiplicity multiplicity, int tokenType, String ruleName) {
		super(grammar, label, multiplicity, tokenType);
		
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

}
