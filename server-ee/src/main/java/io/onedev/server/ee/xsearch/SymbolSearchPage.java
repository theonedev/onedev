package io.onedev.server.ee.xsearch;

import io.onedev.server.search.code.query.SymbolQueryOption;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SymbolSearchPage extends CodeSearchPage<SymbolQueryOption> {
	
	public SymbolSearchPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Search Symbols in Default Branch");
	}
	
}
