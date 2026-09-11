package io.onedev.server.web.component.issue.pullrequests;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.pullrequest.RequestStatusBadge;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class IssuePullRequestsPanel extends GenericPanel<Issue> {

	private final IModel<List<PullRequest>> requestsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<PullRequest> load() {
			return getModelObject().getPullRequests().stream()
					.filter(it -> SecurityUtils.canReadCode(it.getTargetProject()))
					.collect(Collectors.toList());
		}
	};

	public IssuePullRequestsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ListView<PullRequest>("pullRequests", requestsModel) {
			@Override
			protected void populateItem(ListItem<PullRequest> item) {
				var request = item.getModelObject();
				var link = new BookmarkablePageLink<Void>("link", PullRequestActivitiesPage.class,
						PullRequestActivitiesPage.paramsOf(request));
				link.add(new Label("title", request.getTitle() + " ("
						+ request.getReference().toString(IssuePullRequestsPanel.this.getModelObject().getProject()) + ")"));
				item.add(link);
				item.add(new RequestStatusBadge("status", item.getModel()));
			}
		});
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!requestsModel.getObject().isEmpty());
	}

	@Override
	protected void onDetach() {
		requestsModel.detach();
		super.onDetach();
	}
}
