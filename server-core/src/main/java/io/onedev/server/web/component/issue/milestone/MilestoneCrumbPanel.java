package io.onedev.server.web.component.issue.milestone;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.editable.InplacePropertyEditLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneIssuesPage;
import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.io.Serializable;

import static java.util.stream.Collectors.toList;

public abstract class MilestoneCrumbPanel extends Panel {

	public MilestoneCrumbPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var editLink = new InplacePropertyEditLink("edit") {

			protected Component newContent(String id, FloatingPanel dropdown) {
				if (!SecurityUtils.canScheduleIssues(getIssue().getProject())) {
					return new Label(id, "<div class='px-3 py-2'><i>No permission to schedule issues</i></div>")
							.setEscapeModelStrings(false);
				} else {
					return super.newContent(id, dropdown);
				}
			}

			@Override
			protected Serializable getBean() {
				var bean = new MilestonesBean();
				bean.setMilestoneNames(getIssue().getSchedules().stream()
						.map(it -> it.getMilestone().getName()).collect(toList()));
				return bean;
			}

			@Override
			protected String getPropertyName() {
				return "milestoneNames";
			}

			@Override
			protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName) {
				var milestonesBean = (MilestonesBean) bean;
				var issue = getIssue();
				var milestones = milestonesBean.getMilestoneNames().stream()
						.map(it -> issue.getProject().getHierarchyMilestone(it))
						.collect(toList());
				OneDev.getInstance(IssueChangeManager.class).changeMilestones(issue, milestones);
				((BasePage) getPage()).notifyObservablesChange(handler, issue.getChangeObservables(true));
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}
			
		};
		add(editLink);
	
		var milestonesView = new RepeatingView("milestones");
		for (var schedule: getIssue().getSchedules()) {
			var child = new BookmarkablePageLink<Void>(milestonesView.newChildId(),
					MilestoneIssuesPage.class,
					MilestoneIssuesPage.paramsOf(getIssue().getProject(), schedule.getMilestone()));
			milestonesView.add(child);
			child.add(new Label("name", schedule.getMilestone().getName()));
		}
		editLink.add(milestonesView);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getIssue().getSchedules().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestoneCrumbCssResourceReference()));
	}

	protected abstract Issue getIssue();
}
