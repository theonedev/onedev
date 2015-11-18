package com.pmease.commons.antlr.grammarspec;

public class TokenElementSpec extends ElementSpec {

	private final int type;

	public TokenElementSpec(String label, Multiplicity multiplicity, int type) {
		super(label, multiplicity);
		
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
