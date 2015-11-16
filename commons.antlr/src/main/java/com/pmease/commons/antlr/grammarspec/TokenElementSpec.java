package com.pmease.commons.antlr.grammarspec;

public class TokenElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;

	private final int type;

	public TokenElementSpec(Grammar grammar, String label, Multiplicity multiplicity, int type) {
		super(grammar, label, multiplicity);
		
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
