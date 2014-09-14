package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;

public class UpdatePullRequest implements PullRequestActivity {

	private final PullRequestUpdate update;
	
	public UpdatePullRequest(PullRequestUpdate update) {
		this.update = update;
	}
	
	@Override
	public Panel render(String panelId) {
		return new UpdateActivityPanel(panelId, new PullRequestUpdateModel(update.getId()));
	}

	@Override
	public Date getDate() {
		return update.getDate();
	}

	@Override
	public User getUser() {
		return update.getUser();
	}

	public PullRequestUpdate getUpdate() {
		return update;
	}

}
