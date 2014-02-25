package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;

@Singleton
public class DefaultVoteManager extends AbstractGenericDao<Vote> implements VoteManager {

	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultVoteManager(GeneralDao generalDao, PullRequestManager pullRequestManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
	}

	@Transactional
	@Override
	public void save(Vote vote) {
		super.save(vote);

		if (vote.getUpdate().getRequest().isOpen())
			pullRequestManager.refresh(vote.getUpdate().getRequest());
	}

	@Transactional
	@Override
	public void delete(Vote vote) {
		super.delete(vote);

		vote.getVoter().getVotes().remove(vote);
		vote.getUpdate().getVotes().remove(vote);

		if (vote.getUpdate().getRequest().isOpen())
			pullRequestManager.refresh(vote.getUpdate().getRequest());
	}

	@Sessional
	@Override
	public Vote find(User reviewer, PullRequestUpdate update) {
		return find(new Criterion[]{Restrictions.eq("reviewer", reviewer), Restrictions.eq("update", update)});
	}

}
