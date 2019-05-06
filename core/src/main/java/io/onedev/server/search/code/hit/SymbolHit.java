package io.onedev.server.search.code.hit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.utils.LinearRange;

public class SymbolHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Symbol symbol;
	
	private final LinearRange match;
	
	public SymbolHit(String blobPath, Symbol symbol, @Nullable LinearRange match) {
		super(blobPath, symbol.getPosition());
		this.symbol = symbol;
		this.match = match;
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
		return symbol.render(componentId, match);
	}

	@Override
	public Image renderIcon(String componentId) {
		return symbol.renderIcon(componentId);
	}
	
	@Override
	public String getNamespace() {
		if (symbol.getParent() != null)
			return symbol.getParent().getFQN();
		else
			return null;
	}

}
