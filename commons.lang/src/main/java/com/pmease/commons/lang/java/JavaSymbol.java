package com.pmease.commons.lang.java;

import com.pmease.commons.lang.Symbol;
import com.pmease.commons.lang.TokenPosition;

public abstract class JavaSymbol extends Symbol {

	private static final long serialVersionUID = 1L;

	public JavaSymbol(Symbol parent, String name, TokenPosition pos) {
		super(parent, name, pos);
	}

}
