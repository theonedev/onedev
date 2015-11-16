package com.pmease.commons.antlr.grammarspec;

import java.util.List;

public class RuleSpec extends Spec {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<AlternativeSpec> altenatives;

	public RuleSpec(Grammar grammar, String name, List<AlternativeSpec> altenatives) {
		super(grammar);
		
		this.name = name;
		this.altenatives = altenatives;
	}
	
	public String getName() {
		return name;
	}

	public List<AlternativeSpec> getAlternatives() {
		return altenatives;
	}

}
