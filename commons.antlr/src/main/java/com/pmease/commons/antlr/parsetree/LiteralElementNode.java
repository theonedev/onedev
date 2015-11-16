package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.LiteralElementSpec;

public class LiteralElementNode extends TokenElementNode {

	private static final long serialVersionUID = 1L;

	public LiteralElementNode(LiteralElementSpec spec, AlternativeNode parent) {
		super(spec, parent, spec.getLiteral());
	}

}
