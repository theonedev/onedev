package com.pmease.commons.antlr.grammarspec;

import java.util.List;

public class BlockElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;

	private final List<AlternativeSpec> altenatives;

	public BlockElementSpec(Grammar grammar, String label, Multiplicity multiplicity, List<AlternativeSpec> altenatives) {
		super(grammar, label, multiplicity);
		
		this.altenatives = altenatives;
	}
	
	public List<AlternativeSpec> getAltenatives() {
		return altenatives;
	}

}
