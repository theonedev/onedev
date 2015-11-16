package com.pmease.commons.antlr.grammar;

import java.util.List;

public class Rule extends GrammarMember {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<Altenative> altenatives;

	public Rule(Grammar grammar, String name, List<Altenative> altenatives) {
		super(grammar);
		
		this.name = name;
		this.altenatives = altenatives;
	}
	
	public String getName() {
		return name;
	}

	public List<Altenative> getAltenatives() {
		return altenatives;
	}

}
