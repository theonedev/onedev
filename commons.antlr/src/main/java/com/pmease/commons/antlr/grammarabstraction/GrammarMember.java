package com.pmease.commons.antlr.grammarabstraction;

import java.io.Serializable;

public class GrammarMember implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Grammar grammar;
	
	public GrammarMember(Grammar grammar) {
		this.grammar = grammar;
	}

	public Grammar getGrammar() {
		return grammar;
	}
	
}
