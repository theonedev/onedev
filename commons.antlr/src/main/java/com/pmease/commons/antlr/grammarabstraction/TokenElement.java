package com.pmease.commons.antlr.grammarabstraction;

public class TokenElement extends Element {

	private static final long serialVersionUID = 1L;

	private final int type;

	public TokenElement(Grammar grammar, String label, Multiplicity multiplicity, int type) {
		super(grammar, label, multiplicity);
		
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
