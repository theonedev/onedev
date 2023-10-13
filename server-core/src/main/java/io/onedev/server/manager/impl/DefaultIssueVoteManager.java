package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.manager.IssueVoteManager;
import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.manager.IssueWorkManager;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueVoteManager extends BaseEntityManager<IssueVote>
		implements IssueVoteManager {

	private final IssueWatchManager watchManager;
	
	@Inject
	public DefaultIssueVoteManager(Dao dao, IssueWatchManager watchManager) {
		super(dao);
		this.watchManager = watchManager;
	}

	@Transactional
	@Override
	public void create(IssueVote vote) {
		Preconditions.checkState(vote.isNew());
		vote.getIssue().setVoteCount(vote.getIssue().getVoteCount()+1);
		dao.persist(vote);
		watchManager.watch(vote.getIssue(), vote.getUser(), true);
	}

	@Transactional
	@Override
	public void delete(IssueVote vote) {
		super.delete(vote);
		vote.getIssue().setVoteCount(vote.getIssue().getVoteCount()-1);
	}

}
