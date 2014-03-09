package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.PullRequest;

public class ClosePullRequest implements PullRequestActivity {

	private final PullRequest request;
	
	public ClosePullRequest(PullRequest request) {
		this.request = request;
	}
	
	@Override
	public Panel render(String panelId) {
		return new CloseActivityPanel(panelId, new PullRequestModel(request.getId()));
	}

	@Override
	public Date getDate() {
		return request.getCloseInfo().getCloseDate();
	}

}
