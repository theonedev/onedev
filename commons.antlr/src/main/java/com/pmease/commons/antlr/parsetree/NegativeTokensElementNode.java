package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.NotTokenElementSpec;

public class NegativeTokensElementNode extends AltTokenElementNode {

	private static final long serialVersionUID = 1L;
	
	public NegativeTokensElementNode(NotTokenElementSpec spec, AlternativeNode parent, 
			int tokenType, String tokenValue) {
		super(spec, parent, tokenType, tokenValue);
	}

}
