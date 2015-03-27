package com.pmease.gitplex.search.hit;

import org.apache.wicket.Component;

import com.pmease.commons.lang.Symbol;

public class SymbolHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Symbol symbol;
	
	public SymbolHit(String blobPath, Symbol symbol) {
		super(blobPath);
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol.toString();
	}

	@Override
	public Component render(String componentId) {
		return symbol.render(componentId);
	}

	@Override
	public int getLineNo() {
		return symbol.lineNo;
	}

}
