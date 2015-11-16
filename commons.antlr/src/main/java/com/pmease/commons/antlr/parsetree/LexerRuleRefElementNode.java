package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.grammarspec.RuleSpec;

public class LexerRuleRefElementNode extends TokenElementNode {

	private static final long serialVersionUID = 1L;

	private final RuleSpec ruleSpec;
	
	public LexerRuleRefElementNode(LexerRuleRefElementSpec spec, AlternativeNode parent, String value) {
		super(spec, parent, value);
		
		ruleSpec = spec.getGrammar().getRules().get(spec.getRuleName());
	}

	public RuleSpec getRuleSpec() {
		return ruleSpec;
	}

}
