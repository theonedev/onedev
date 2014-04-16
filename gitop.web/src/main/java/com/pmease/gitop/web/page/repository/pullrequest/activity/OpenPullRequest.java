package com.pmease.gitop.web.page.repository.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

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
		return request.getSubmittedBy();
	}

	@Override
	public String getAction() {
		return "Opened";
	}

}
