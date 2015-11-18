package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.LexerRuleRefElementSpec;

public class LexerRuleRefElementNode extends TokenElementNode {

	public LexerRuleRefElementNode(LexerRuleRefElementSpec spec, AlternativeNode parent, String value) {
		super(spec, parent, value);
	}

}
