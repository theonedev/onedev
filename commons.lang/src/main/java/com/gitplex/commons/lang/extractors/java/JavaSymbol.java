package com.gitplex.commons.lang.extractors.java;

import com.gitplex.commons.lang.extractors.Symbol;
import com.gitplex.commons.lang.extractors.TokenPosition;

public abstract class JavaSymbol extends Symbol {

	private static final long serialVersionUID = 1L;

	public JavaSymbol(Symbol parent, String name, TokenPosition pos) {
		super(parent, name, pos);
	}

}
