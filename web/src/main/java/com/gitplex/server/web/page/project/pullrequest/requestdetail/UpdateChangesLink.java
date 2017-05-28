package com.gitplex.server.web.page.project.pullrequest.requestdetail;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.changes.RequestChangesPage;

@SuppressWarnings("serial")
public class UpdateChangesLink extends ViewStateAwarePageLink<Void> {
	
	public UpdateChangesLink(String id, PullRequestUpdate update) {
		super(id, RequestChangesPage.class, paramsOf(update));
		
		setEscapeModelStrings(false);
	}

	private static PageParameters paramsOf(PullRequestUpdate update) {
		return RequestChangesPage.paramsOf(update.getRequest(), update.getBaseCommitHash(), update.getHeadCommitHash());
	}

	public IModel<?> getBody() {
		return Model.of("<i class='fa fa-ext fa-file-diff'></i>");
	}
	
}
