package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.RuleSpec;

public class RuleNode extends Node {

	public RuleNode(RuleSpec spec, RuleRefElementNode parent) {
		super(spec, parent);
	}

}
