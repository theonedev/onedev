package io.onedev.server.web.page.project.pullrequests.detail;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@SuppressWarnings("serial")
public class UpdateChangesLink extends ViewStateAwarePageLink<Void> {
	
	public UpdateChangesLink(String id, PullRequestUpdate update) {
		super(id, PullRequestChangesPage.class, paramsOf(update));
		
		setEscapeModelStrings(false);
	}

	private static PageParameters paramsOf(PullRequestUpdate update) {
		return PullRequestChangesPage.paramsOf(update.getRequest(), 
				update.getBaseCommitHash(), update.getHeadCommitHash());
	}

	public IModel<?> getBody() {
		return Model.of("<i class='fa fa-ext fa-file-diff'></i>");
	}
	
}
