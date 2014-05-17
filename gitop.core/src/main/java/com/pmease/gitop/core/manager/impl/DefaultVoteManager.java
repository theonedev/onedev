package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.Vote.Result;

@Singleton
public class DefaultVoteManager implements VoteManager {

	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultVoteManager(Dao dao, PullRequestManager pullRequestManager) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
	}

	@Sessional
	@Override
	public Vote find(User reviewer, PullRequestUpdate update) {
		return dao.find(EntityCriteria.of(Vote.class)
				.add(Restrictions.eq("voter", reviewer)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void vote(PullRequest request, User user, Result result, String comment) {
		Vote vote = new Vote();
		vote.setResult(result);
		vote.setUpdate(request.getLatestUpdate());
		vote.setVoter(user);
		vote.setComment(comment);
		
		vote.getVoter().getVotes().add(vote);
		vote.getUpdate().getVotes().add(vote);
		dao.persist(vote);		

		pullRequestManager.refresh(request);
	}

}
