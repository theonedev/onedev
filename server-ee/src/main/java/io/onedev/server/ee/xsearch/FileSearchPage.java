package io.onedev.server.ee.xsearch;

import io.onedev.server.search.code.query.FileQueryOption;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class FileSearchPage extends CodeSearchPage<FileQueryOption> {
	
	public FileSearchPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Search Files in Default Branch");
	}
	
}
