package io.onedev.server.web.page.project.pullrequests.detail;

import javax.annotation.Nullable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.util.Cursor;

@SuppressWarnings("serial")
public class UpdateChangesLink extends ViewStateAwarePageLink<Void> {
	
	public UpdateChangesLink(String id, PullRequestUpdate update, @Nullable Cursor cursor) {
		super(id, PullRequestChangesPage.class, paramsOf(update, cursor));
		
		setEscapeModelStrings(false);
	}

	private static PageParameters paramsOf(PullRequestUpdate update, @Nullable Cursor cursor) {
		return PullRequestChangesPage.paramsOf(update.getRequest(), cursor, update.getBaseCommitHash(), update.getHeadCommitHash());
	}

	public IModel<?> getBody() {
		return Model.of("<i class='fa fa-ext fa-file-diff'></i>");
	}
	
}
