package com.gitplex.server.search.hit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.commons.lang.extractors.Symbol;
import com.gitplex.commons.util.Range;

public class SymbolHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Symbol symbol;
	
	private final Range matchRange;
	
	public SymbolHit(String blobPath, Symbol symbol, @Nullable Range matchRange) {
		super(blobPath, symbol.getPos());
		this.symbol = symbol;
		this.matchRange = matchRange;
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
		return symbol.render(componentId, matchRange);
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
