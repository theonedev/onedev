package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.model.PullRequest;

@SuppressWarnings("serial")
public class PullRequestModel extends LoadableDetachableModel<PullRequest> {

	private final Long requestId;
	
	public PullRequestModel(Long requestId) {
		this.requestId = requestId;
	}
	
	@Override
	protected PullRequest load() {
		return GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
	}

}
