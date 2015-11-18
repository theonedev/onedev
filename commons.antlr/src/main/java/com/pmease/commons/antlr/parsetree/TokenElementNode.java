package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.TokenElementSpec;

public class TokenElementNode extends ElementNode {

	private final String value;
	
	public TokenElementNode(TokenElementSpec spec, AlternativeNode parent, String value) {
		super(spec, parent);
		
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
