package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.LiteralElementSpec;

public class LiteralElementNode extends TokenElementNode {

	public LiteralElementNode(LiteralElementSpec spec, AlternativeNode parent) {
		super(spec, parent, spec.getLiteral());
	}

}
