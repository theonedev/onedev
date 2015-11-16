package com.pmease.commons.antlr.parsetree;

import java.util.ArrayList;

import com.pmease.commons.antlr.grammarabstraction.TokenElement;

public class TokenNode extends Node {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TokenNode(TokenElement tokenElement, Node parent, String value) {
		super(tokenElement, parent, new ArrayList<Node>());
		
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
