package com.pmease.gitop.web.page.project.pullrequest.activity;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.PullRequest;

@SuppressWarnings("serial")
public class PullRequestModel extends LoadableDetachableModel<PullRequest> {

	private final Long requestId;
	
	public PullRequestModel(Long requestId) {
		this.requestId = requestId;
	}
	
	@Override
	protected PullRequest load() {
		return Gitop.getInstance(PullRequestManager.class).load(requestId);
	}

}
