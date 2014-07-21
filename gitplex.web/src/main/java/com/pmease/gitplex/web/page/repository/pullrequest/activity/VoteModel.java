package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.model.Vote;

@SuppressWarnings("serial")
public class VoteModel extends LoadableDetachableModel<Vote> {

	private final Long voteId;
	
	public VoteModel(Long voteId) {
		this.voteId = voteId;
	}
	
	@Override
	protected Vote load() {
		return GitPlex.getInstance(Dao.class).load(Vote.class, voteId);
	}

}
