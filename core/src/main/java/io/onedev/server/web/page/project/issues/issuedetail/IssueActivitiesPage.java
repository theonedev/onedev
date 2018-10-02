package io.onedev.server.web.page.project.issues.issuedetail;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Issue;
import io.onedev.server.web.component.issue.activities.IssueActivitiesPanel;

@SuppressWarnings("serial")
public class IssueActivitiesPage extends IssueDetailPage {

	public IssueActivitiesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new IssueActivitiesPanel("activities") {

			@Override
			protected Issue getIssue() {
				return IssueActivitiesPage.this.getIssue();
			}
			
		});
	}

}
