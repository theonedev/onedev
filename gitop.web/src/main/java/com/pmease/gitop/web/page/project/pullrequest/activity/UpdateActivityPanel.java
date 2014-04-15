package com.pmease.gitop.web.page.project.pullrequest.activity;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.web.page.project.pullrequest.UpdateCommitsPanel;

@SuppressWarnings("serial")
public class UpdateActivityPanel extends Panel {

	private IModel<PullRequestUpdate> model;
	
	public UpdateActivityPanel(String id, IModel<PullRequestUpdate> model) {
		super(id);
		
		this.model = model;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UpdateCommitsPanel("commits", model));
	}

}
