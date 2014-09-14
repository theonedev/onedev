package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

public class OpenPullRequest implements PullRequestActivity {

	private final PullRequest request;
	
	public OpenPullRequest(PullRequest request) {
		this.request = request;
	}
	
	@Override
	public Panel render(String panelId) {
		return new OpenActivityPanel(panelId, new PullRequestModel(request.getId()));
	}

	@Override
	public Date getDate() {
		return request.getCreateDate();
	}

	@Override
	public User getUser() {
		return request.getSubmitter();
	}

}
