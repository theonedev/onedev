package com.pmease.commons.antlr.grammar;

import java.io.Serializable;

public class GrammarAware implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Grammar grammar;
	
	public GrammarAware(Grammar grammar) {
		this.grammar = grammar;
	}

	public Grammar getGrammar() {
		return grammar;
	}
	
}
