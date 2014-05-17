package com.pmease.gitop.web.page.repository.pullrequest.activity;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Vote;

@SuppressWarnings("serial")
public class VoteModel extends LoadableDetachableModel<Vote> {

	private final Long voteId;
	
	public VoteModel(Long voteId) {
		this.voteId = voteId;
	}
	
	@Override
	protected Vote load() {
		return Gitop.getInstance(Dao.class).load(Vote.class, voteId);
	}

}
