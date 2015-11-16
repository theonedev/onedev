package com.pmease.commons.antlr.grammarspec;

import java.util.Set;

public class NotTokenElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final Set<Integer> notTokenTypes;
	
	public NotTokenElementSpec(Grammar grammar, String label, Multiplicity multiplicity, 
			Set<Integer> notTokenTypes) {
		super(grammar, label, multiplicity);
		
		this.notTokenTypes = notTokenTypes;
	}

	public Set<Integer> getNotTokenTypes() {
		return notTokenTypes;
	}

}
