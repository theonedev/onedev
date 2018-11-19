package io.onedev.server.web.page.project.issues.detail;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Issue;
import io.onedev.server.web.component.issue.activities.IssueActivitiesPanel;

@SuppressWarnings("serial")
public class IssueActivitiesPage extends IssueDetailPage {

	private IssueActivitiesPanel activities;
	
	public IssueActivitiesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(activities = new IssueActivitiesPanel("activities") {

			@Override
			protected Issue getIssue() {
				return IssueActivitiesPage.this.getIssue();
			}
			
		});
	}

	public Component renderOptions(String componentId) {
		return activities.renderOptions(componentId);
	}
	
}
