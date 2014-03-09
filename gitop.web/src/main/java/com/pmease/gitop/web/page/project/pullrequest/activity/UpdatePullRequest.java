package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.PullRequestUpdate;

public class UpdatePullRequest implements PullRequestActivity {

	private final PullRequestUpdate update;
	
	public UpdatePullRequest(PullRequestUpdate update) {
		this.update = update;
	}
	
	@Override
	public Panel render(String panelId) {
		return null;
	}

	@Override
	public Date getDate() {
		return update.getDate();
	}

}
