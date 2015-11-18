package com.pmease.commons.antlr.grammarspec;

import java.util.List;

public class RuleSpec implements Spec {

	private final String name;
	
	private final List<AlternativeSpec> altenatives;

	public RuleSpec(String name, List<AlternativeSpec> altenatives) {
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
