package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.milestone.burndown.MilestoneBurndownPanel;

@SuppressWarnings("serial")
public class MilestoneBurndownPage extends MilestoneDetailPage {

	public MilestoneBurndownPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new MilestoneBurndownPanel("burndown", milestoneModel));
	}		
	
}
