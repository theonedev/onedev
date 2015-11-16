package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.RuleRefElementSpec;

public class RuleRefElementNode extends ElementNode {

	private static final long serialVersionUID = 1L;

	private final RuleNode rule;
	
	public RuleRefElementNode(RuleRefElementSpec spec, AlternativeNode parent, RuleNode rule) {
		super(spec, parent);
		
		this.rule = rule;
	}

	public RuleNode getRule() {
		return rule;
	}

}
