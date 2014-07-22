package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.page.repository.info.pullrequest.UpdateCommitsPanel;

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
