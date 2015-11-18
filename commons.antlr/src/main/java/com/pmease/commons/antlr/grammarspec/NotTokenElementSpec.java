package com.pmease.commons.antlr.grammarspec;

import java.util.Set;

public class NotTokenElementSpec extends ElementSpec {

	private final Set<Integer> notTokenTypes;
	
	public NotTokenElementSpec(String label, Multiplicity multiplicity, Set<Integer> notTokenTypes) {
		super(label, multiplicity);
		
		this.notTokenTypes = notTokenTypes;
	}

	public Set<Integer> getNotTokenTypes() {
		return notTokenTypes;
	}

}
