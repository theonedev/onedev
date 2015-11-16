package com.pmease.commons.antlr.grammar;

public class Element extends GrammarMember {
	
	private static final long serialVersionUID = 1L;

	public enum Multiplicity{ONE, ONE_OR_ZERO, ZERO_OR_MORE, ONE_OR_MORE};
	
	private final String label;
	
	private final Multiplicity multiplicity;
	
	public Element(Grammar grammar, String label, Multiplicity multiplicity) {
		super(grammar);
		
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
