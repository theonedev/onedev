package com.pmease.commons.antlr.codeassist;

import org.antlr.v4.runtime.Token;

public class TokenNode extends Node {

	public TokenNode(ElementSpec spec, Node parent, Token token) {
		super(spec, parent, token);
	}

}
