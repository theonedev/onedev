package com.pmease.commons.antlr.parsetree;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.grammarspec.ElementSpec;

public class TokenNode extends Node {

	private final Token token;
	
	public TokenNode(ElementSpec spec, Node parent, Token token) {
		super(spec, parent);
		
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

}
