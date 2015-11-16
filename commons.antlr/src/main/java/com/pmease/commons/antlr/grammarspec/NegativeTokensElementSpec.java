package com.pmease.commons.antlr.grammarspec;

import java.util.Set;

public class NegativeTokensElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final Set<Integer> negativeTokenTypes;
	
	public NegativeTokensElementSpec(Grammar grammar, String label, Multiplicity multiplicity, 
			Set<Integer> negativeTokenTypes) {
		super(grammar, label, multiplicity);
		
		this.negativeTokenTypes = negativeTokenTypes;
	}

	public Set<Integer> getTokenType() {
		return negativeTokenTypes;
	}

}
