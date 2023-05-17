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
		return new Label(componentId, "<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>")
				.setEscapeModelStrings(false);
	}
	
}
