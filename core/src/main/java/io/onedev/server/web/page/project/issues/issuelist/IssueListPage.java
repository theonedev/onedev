package io.onedev.server.web.page.project.issues.issuelist;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;

@SuppressWarnings("serial")
public class IssueListPage extends ProjectPage {

	public IssueListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())));
	}
	
}
