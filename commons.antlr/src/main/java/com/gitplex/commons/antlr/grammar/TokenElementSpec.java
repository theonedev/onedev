package com.gitplex.commons.antlr.grammar;

public abstract class TokenElementSpec extends TerminalElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final int tokenType;
	
	public TokenElementSpec(String label, Multiplicity multiplicity, int tokenType) {
		super(label, multiplicity);
		
		this.tokenType = tokenType;
	}

	public int getTokenType() {
		return tokenType;
	}

	@Override
	public boolean isToken(int tokenType) {
		return tokenType == this.tokenType;
	}
	
}
