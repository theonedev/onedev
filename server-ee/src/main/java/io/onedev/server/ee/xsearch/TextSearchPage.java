package io.onedev.server.ee.xsearch;

import io.onedev.server.search.code.query.TextQueryOption;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TextSearchPage extends CodeSearchPage<TextQueryOption> {
	
	public TextSearchPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Search Text in Default Branch");
	}
	
}
