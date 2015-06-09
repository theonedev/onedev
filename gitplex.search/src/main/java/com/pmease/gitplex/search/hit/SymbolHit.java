package com.pmease.gitplex.search.hit;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.commons.lang.Symbol;

public class SymbolHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Symbol symbol;
	
	public SymbolHit(String blobPath, Symbol symbol) {
		super(blobPath, symbol.getPos());
		this.symbol = symbol;
	}

	public Symbol getSymbol() {
		return symbol;
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
	public ResourceReference getIcon() {
		return symbol.getIcon();
	}

	@Override
	public String getScope() {
		return symbol.getScope();
	}

	@Override
	protected int score() {
		return symbol.score();
	}

}
