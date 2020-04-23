package io.onedev.server.web.component.issue.pullrequests;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.pullrequest.IncludesIssueCriteria;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.web.component.pullrequest.list.PullRequestListPanel;

@SuppressWarnings("serial")
public class IssuePullRequestsPanel extends GenericPanel<Issue> {

	public IssuePullRequestsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new PullRequestListPanel("pullRequests", Model.of((String)null)) {

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

			@Override
			protected PullRequestQuery getBaseQuery() {
				String value = String.valueOf(getIssue().getNumber());
				return new PullRequestQuery(new IncludesIssueCriteria(getProject(), value));
			}
			
		});
		
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
