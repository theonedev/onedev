package io.onedev.server.web.page.project.pullrequests.requestdetail;

import javax.annotation.Nullable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.requestdetail.changes.RequestChangesPage;
import io.onedev.server.web.util.QueryPosition;

@SuppressWarnings("serial")
public class UpdateChangesLink extends ViewStateAwarePageLink<Void> {
	
	public UpdateChangesLink(String id, PullRequestUpdate update, @Nullable QueryPosition position) {
		super(id, RequestChangesPage.class, paramsOf(update, position));
		
		setEscapeModelStrings(false);
	}

	private static PageParameters paramsOf(PullRequestUpdate update, @Nullable QueryPosition position) {
		return RequestChangesPage.paramsOf(update.getRequest(), position, update.getBaseCommitHash(), update.getHeadCommitHash());
	}

	public IModel<?> getBody() {
		return Model.of("<i class='fa fa-ext fa-file-diff'></i>");
	}
	
}
