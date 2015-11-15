package com.pmease.commons.antlr;

public class Element {
	
	public enum Multiplicity{ONE_OR_ZERO, ZERO_OR_MORE, ONE_OR_MORE};
	
	private final Multiplicity multiplicity;
	
	public Element(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	
}
