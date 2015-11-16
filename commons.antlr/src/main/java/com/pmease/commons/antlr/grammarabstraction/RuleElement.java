package com.pmease.commons.antlr.grammarabstraction;

public class RuleElement extends Element {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	public RuleElement(Grammar grammar, String label, Multiplicity multiplicity, String name) {
		super(grammar, label, multiplicity);
		
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
