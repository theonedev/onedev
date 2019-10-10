package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.web.component.milestone.progress.MilestoneProgressBar;

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
		
		add(new MilestoneProgressBar("progress", getModel()));
		
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();		
		IssueCriteria openCriteria = issueSetting.getCategoryCriteria(StateSpec.Category.OPEN);
		Link<Void> link = new BookmarkablePageLink<Void>("open", MilestoneDetailPage.class, 
				MilestoneDetailPage.paramsOf(getMilestone(), openCriteria.toString()));
		link.add(new Label("count", getMilestone().getNumOfOpenIssues() + " open"));
		add(link);
		
		IssueCriteria closedCriteria = issueSetting.getCategoryCriteria(StateSpec.Category.CLOSED);
		link = new BookmarkablePageLink<Void>("closed",  MilestoneDetailPage.class, 
				MilestoneDetailPage.paramsOf(getMilestone(), closedCriteria.toString()));
		link.add(new Label("count", getMilestone().getNumOfClosedIssues() + " closed"));
		add(link);
	}

}
