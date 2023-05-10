package io.onedev.server.web.page.codesearch;

import io.onedev.server.web.page.layout.LayoutPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class CodeSearchPage extends LayoutPage {
	
	public CodeSearchPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Code Search");
	}
}
