package io.onedev.server.ee.xsearch.match;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.utils.LinearRange;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

public class SymbolPanel extends Panel {
	
	private final Symbol symbol;
	
	public SymbolPanel(String id, Symbol symbol) {
		super(id);
		this.symbol = symbol;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(symbol.render("declare", new LinearRange(0, symbol.getName().length())));
		add(new Label("namespace", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				if (symbol.getParent() != null)
					return symbol.getParent().getFQN();
				else
					return null;
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getDefaultModelObject() != null);
			}
		});
	}
}
