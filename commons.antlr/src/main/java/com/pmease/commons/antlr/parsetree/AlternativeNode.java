package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.AlternativeSpec;

public class AlternativeNode extends Node {

	public AlternativeNode(AlternativeSpec spec, RuleNode parent) {
		super(spec, parent);
	}

}