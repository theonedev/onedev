package com.gitplex.server.search.hit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;

public class SymbolHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Symbol symbol;
	
	private final Range matchRange;
	
	public SymbolHit(String blobPath, Symbol symbol, @Nullable Range matchRange) {
		super(blobPath, symbol.getPosition());
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
	public Image renderIcon(String componentId) {
		return symbol.renderIcon(componentId);
	}
	
	@Override
	public String getNamespace() {
		return symbol.getNamespace();
	}

}
