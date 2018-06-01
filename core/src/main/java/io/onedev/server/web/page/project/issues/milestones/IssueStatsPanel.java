package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.issue.query.IssueCriteria;

@SuppressWarnings("serial")
class IssueStatsPanel extends GenericPanel<Milestone> {

	public IssueStatsPanel(String id, IModel<Milestone> model) {
		super(id, model);
	}

	private Milestone getMilestone() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		int progress;
		int totalIssues = getMilestone().getNumOfOpenIssues()+getMilestone().getNumOfClosedIssues();
		if (totalIssues != 0)
			progress = 100 * getMilestone().getNumOfClosedIssues() / totalIssues;
		else
			progress = 0;
		if (progress > 100)
			progress = 100;
		
		add(new WebMarkupContainer("progress").add(AttributeAppender.append("style", "width: " + progress + "%;")));
		
		IssueCriteria openCriteria = getMilestone().getProject().getIssueWorkflow().getStatesCriteria(false);
		if (openCriteria != null) {
			Link<Void> link = new BookmarkablePageLink<Void>("open", MilestoneDetailPage.class, 
					MilestoneDetailPage.paramsOf(getMilestone(), openCriteria.toString()));
			link.add(new Label("count", getMilestone().getNumOfOpenIssues() + " open"));
			add(link);
		} else {
			WebMarkupContainer link = new WebMarkupContainer("open") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
			link.add(new Label("count", getMilestone().getNumOfOpenIssues() + " open"));
			add(link);
		}
		
		IssueCriteria closedCriteria = getMilestone().getProject().getIssueWorkflow().getStatesCriteria(true);
		if (closedCriteria != null) {
			Link<Void> link = new BookmarkablePageLink<Void>("closed",  MilestoneDetailPage.class, 
					MilestoneDetailPage.paramsOf(getMilestone(), closedCriteria.toString()));
			link.add(new Label("count", getMilestone().getNumOfClosedIssues() + " closed"));
			add(link);
		} else {
			WebMarkupContainer link = new WebMarkupContainer("closed") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
			link.add(new Label("count", getMilestone().getNumOfOpenIssues() + " closed"));
			add(link);
		}
	}

}
