package io.onedev.server.web.component.issue.pullrequests;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

@SuppressWarnings("serial")
public class IssuePullRequestsPanel extends GenericPanel<Issue> {

	public IssuePullRequestsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequest>("pullRequests", new AbstractReadOnlyModel<List<PullRequest>>() {

			@Override
			public List<PullRequest> getObject() {
				return getIssue().getPullRequests();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PullRequest> item) {
				PullRequest request = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>("title", PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(request, null));
				link.add(new Label("label",  "#" + request.getNumber() + " - " + request.getTitle()));
				item.add(link);
				item.add(new RequestStatusLabel("status", item.getModel()));
			}}
		
		);
	}

	private Issue getIssue() {
		return getModelObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssuePullRequestsCssResourceReference()));
	}

}
