package com.pmease.commons.antlr;

public class Element {
	
	public enum Multiplicity{ONE_OR_ZERO, ZERO_OR_MORE, ONE_OR_MORE};
	
	private final String label;
	
	private final Multiplicity multiplicity;
	
	public Element(String label, Multiplicity multiplicity) {
		this.label = label;
		this.multiplicity = multiplicity;
	}

	public String getLabel() {
		return label;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	
}
