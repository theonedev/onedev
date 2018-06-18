package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;

@SuppressWarnings("serial")
abstract class BoardCardPanel extends Panel {

	public BoardCardPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Link<Void> link = new BookmarkablePageLink<Void>("number", 
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(getIssue(), null));
		link.add(new Label("label", "#" + getIssue().getNumber()));
		add(link);
		
		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getIssue().getTitle();
			}
			
		}));
		add(new WebMarkupContainer("state") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				String state = getIssue().getState();
				StateSpec stateSpec = getIssue().getProject().getIssueWorkflow().getStateSpec(state);
				add(AttributeAppender.append("style", "background:" + stateSpec.getColor() + ";"));
				add(AttributeAppender.append("title", state));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	protected abstract Issue getIssue();
}
