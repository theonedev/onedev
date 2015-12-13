package com.pmease.commons.antlr.grammar;

import java.util.List;

import org.antlr.v4.runtime.Token;

public abstract class TerminalElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;

	public TerminalElementSpec(String label, Multiplicity multiplicity) {
		super(label, multiplicity);
	}

	public abstract boolean isToken(int tokenType);

	@Override
	public int getMatchDistance(List<Token> tokens) {
		if (!tokens.isEmpty() && isToken(tokens.get(0).getType()))
			return 1;
		else 
			return -1;
	}
	
}
