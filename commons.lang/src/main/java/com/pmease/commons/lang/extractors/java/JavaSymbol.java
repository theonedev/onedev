package com.pmease.commons.lang.extractors.java;

import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.lang.extractors.TokenPosition;

public abstract class JavaSymbol extends Symbol {

	private static final long serialVersionUID = 1L;

	public JavaSymbol(Symbol parent, String name, TokenPosition pos) {
		super(parent, name, pos);
	}

}
