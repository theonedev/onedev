package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class IssueBoardsPage extends IssuesPage {

	public IssueBoardsPage(PageParameters params) {
		super(params);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueBoardsResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueBoards.onDomReady();"));
	}
	
}
