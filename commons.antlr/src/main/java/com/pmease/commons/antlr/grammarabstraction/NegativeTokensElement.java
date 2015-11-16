package com.pmease.commons.antlr.grammarabstraction;

import java.util.Set;

public class NegativeTokensElement extends Element {

	private static final long serialVersionUID = 1L;
	
	private final Set<Integer> negativeTokenTypes;
	
	public NegativeTokensElement(Grammar grammar, String label, Multiplicity multiplicity, 
			Set<Integer> negativeTokenTypes) {
		super(grammar, label, multiplicity);
		
		this.negativeTokenTypes = negativeTokenTypes;
	}

	public Set<Integer> getTokenType() {
		return negativeTokenTypes;
	}

}
