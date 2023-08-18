package io.onedev.server.ee.xsearch.match;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.utils.PlanarRange;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

public class SymbolMatch implements ContentMatch {

	private static final long serialVersionUID = 1L;

	private final Symbol symbol;
	
	public SymbolMatch(Symbol symbol) {
		this.symbol = symbol;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	@Override
	public Image renderIcon(String componentId) {
		return symbol.renderIcon(componentId);
	}

	@Override
	public Component render(String componentId) {
		return new SymbolPanel(componentId, symbol);
	}

	@Override
	public PlanarRange getPosition() {
		return symbol.getPosition();
	}

}
