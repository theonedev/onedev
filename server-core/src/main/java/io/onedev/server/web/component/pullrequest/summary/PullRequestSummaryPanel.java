package io.onedev.server.web.component.pullrequest.summary;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.QueryPosition;

@SuppressWarnings("serial")
public class PullRequestSummaryPanel extends GenericPanel<PullRequest> {

	public PullRequestSummaryPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getModelObject();
		
		add(new Label("number", "#" + request.getNumber()));
		Link<Void> link = new BookmarkablePageLink<Void>("title", PullRequestActivitiesPage.class, 
				PullRequestActivitiesPage.paramsOf(request, getQueryPosition()));
		link.add(new Label("label", request.getTitle()));
		add(link);
		add(new RequestStatusLabel("status", getModel()));
		UserIdent submitterIdent = UserIdent.of(request.getSubmitter(), request.getSubmitterName());
		add(new UserIdentPanel("submitter", submitterIdent, Mode.NAME));
		add(new Label("submitDate", DateUtils.formatAge(request.getSubmitDate())));
		add(new Label("comments", request.getCommentCount()));
	}

	@Nullable
	protected QueryPosition getQueryPosition() {
		return null;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PullRequestSummaryCssResourceReference()));
	}
	
}
