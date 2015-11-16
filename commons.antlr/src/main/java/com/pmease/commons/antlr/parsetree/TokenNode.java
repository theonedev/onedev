package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.TokenElementSpec;

public class TokenNode extends Node {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TokenNode(TokenElementSpec tokenElement, Node parent, String value) {
		super(tokenElement, parent);
		
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
