package com.pmease.commons.antlr.codeassist;

import org.antlr.v4.runtime.Token;

public class TokenNode extends Node {

	private final Token token;
	
	public TokenNode(ElementSpec spec, Node parent, Node previous, Token token) {
		super(spec, parent, previous);
	
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

	@Override
	public String toString() {
		return "spec: " + spec + ", token: " + token;
	}
	
}
