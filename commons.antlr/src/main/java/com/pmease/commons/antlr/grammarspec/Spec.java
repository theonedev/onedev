package com.pmease.commons.antlr.grammarspec;

import java.io.Serializable;

public class Spec implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Grammar spec;
	
	public Spec(Grammar grammar) {
		this.spec = grammar;
	}

	public Grammar getGrammar() {
		return spec;
	}
	
}
