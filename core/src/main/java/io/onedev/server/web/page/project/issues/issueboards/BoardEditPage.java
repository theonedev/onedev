package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class BoardEditPage extends IssuesPage {

	public BoardEditPage(PageParameters params) {
		super(params);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueBoardsResourceReference()));
	}

}
