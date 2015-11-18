package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.AnyTokenElementSpec;

public class AnyTokenElementNode extends AltTokenElementNode {

	public AnyTokenElementNode(AnyTokenElementSpec spec, AlternativeNode parent, 
			int tokenType, String tokenValue) {
		super(spec, parent, tokenType, tokenValue);
	}

}
