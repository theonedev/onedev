package com.pmease.commons.antlr.grammarabstraction;

import java.util.List;

public class Rule extends GrammarMember {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<Alternative> altenatives;

	public Rule(Grammar grammar, String name, List<Alternative> altenatives) {
		super(grammar);
		
		this.name = name;
		this.altenatives = altenatives;
	}
	
	public String getName() {
		return name;
	}

	public List<Alternative> getAlternatives() {
		return altenatives;
	}

}
