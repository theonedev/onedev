package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.NotTokenElementSpec;

public class NegativeTokensElementNode extends AltTokenElementNode {

	public NegativeTokensElementNode(NotTokenElementSpec spec, AlternativeNode parent, 
			int tokenType, String tokenValue) {
		super(spec, parent, tokenType, tokenValue);
	}

}
