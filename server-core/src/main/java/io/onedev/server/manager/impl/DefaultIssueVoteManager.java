package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.manager.IssueVoteManager;
import io.onedev.server.model.IssueVote;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueVoteManager extends BaseEntityManager<IssueVote>
		implements IssueVoteManager {

	@Inject
	public DefaultIssueVoteManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void create(IssueVote vote) {
		Preconditions.checkState(vote.isNew());
		vote.getIssue().setVoteCount(vote.getIssue().getVoteCount()+1);
		dao.persist(vote);
	}

	@Transactional
	@Override
	public void delete(IssueVote vote) {
		super.delete(vote);
		vote.getIssue().setVoteCount(vote.getIssue().getVoteCount()-1);
	}

}
