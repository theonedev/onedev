package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail;

import static com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare.RequestComparePage.REV_UPDATE_PREFIX;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare.RequestComparePage;

@SuppressWarnings("serial")
public class UpdateChangesLink extends BookmarkablePageLink<Void> {
	
	public UpdateChangesLink(String id, PullRequestUpdate update) {
		super(id, RequestComparePage.class, paramsOf(update));
		
		setEscapeModelStrings(false);
	}

	private static PageParameters paramsOf(PullRequestUpdate update) {
		PullRequest request = update.getRequest();
		int index = request.getSortedUpdates().indexOf(update);
		return RequestComparePage.paramsOf(request, REV_UPDATE_PREFIX+index, REV_UPDATE_PREFIX+(index+1));
	}

	public IModel<?> getBody() {
		return Model.of("<i class='fa fa-ext fa-file-diff'></i>");
	}
	
}
