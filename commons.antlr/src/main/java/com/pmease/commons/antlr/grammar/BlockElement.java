package com.pmease.commons.antlr.grammar;

import java.util.List;

public class BlockElement extends Element {

	private static final long serialVersionUID = 1L;

	private final List<Altenative> altenatives;

	public BlockElement(Grammar grammar, String label, Multiplicity multiplicity, List<Altenative> altenatives) {
		super(grammar, label, multiplicity);
		
		this.altenatives = altenatives;
	}
	
	public List<Altenative> getAltenatives() {
		return altenatives;
	}

}
