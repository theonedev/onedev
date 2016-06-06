package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare.RequestFilesPage;

@SuppressWarnings("serial")
public class UpdateChangesLink extends BookmarkablePageLink<Void> {
	
	public UpdateChangesLink(String id, PullRequestUpdate update) {
		super(id, RequestFilesPage.class, paramsOf(update));
		
		setEscapeModelStrings(false);
	}

	private static PageParameters paramsOf(PullRequestUpdate update) {
		return RequestFilesPage.paramsOf(update.getRequest(), update.getBaseCommitHash(), update.getHeadCommitHash());
	}

	public IModel<?> getBody() {
		return Model.of("<i class='fa fa-ext fa-file-diff'></i>");
	}
	
}
