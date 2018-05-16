package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueVoteManager;
import io.onedev.server.model.IssueVote;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueVoteManager extends AbstractEntityManager<IssueVote>
		implements IssueVoteManager {

	@Inject
	public DefaultIssueVoteManager(Dao dao) {
		super(dao);
	}

	@Transactional
	public void save(IssueVote vote) {
		if (vote.isNew())
			vote.getIssue().setNumOfVotes(vote.getIssue().getNumOfVotes()+1);
		super.save(vote);
	}

	@Transactional
	@Override
	public void delete(IssueVote vote) {
		super.delete(vote);
		vote.getIssue().setNumOfVotes(vote.getIssue().getNumOfVotes()-1);
	}

}
