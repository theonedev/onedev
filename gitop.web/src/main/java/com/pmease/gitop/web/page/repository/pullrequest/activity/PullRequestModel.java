package com.pmease.gitop.web.page.repository.pullrequest.activity;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
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
		return Gitop.getInstance(Dao.class).load(PullRequest.class, requestId);
	}

}
