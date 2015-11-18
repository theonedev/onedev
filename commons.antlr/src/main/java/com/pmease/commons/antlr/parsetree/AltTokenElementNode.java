package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.ElementSpec;

public class AltTokenElementNode extends ElementNode {

	private final int tokenType;
	
	private final String tokenValue;
	
	public AltTokenElementNode(ElementSpec spec, AlternativeNode parent, 
			int tokenType, String tokenValue) {
		super(spec, parent);
		
		this.tokenType = tokenType;
		this.tokenValue = tokenValue;
	}

	public int getTokenType() {
		return tokenType;
	}

	public String getTokenValue() {
		return tokenValue;
	}

}
