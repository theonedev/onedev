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
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.Vote.Result;

@Singleton
public class DefaultVoteManager extends AbstractGenericDao<Vote> implements VoteManager {

	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultVoteManager(GeneralDao generalDao, PullRequestManager pullRequestManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
	}

	@Sessional
	@Override
	public Vote find(User reviewer, PullRequestUpdate update) {
		return find(new Criterion[]{
				Restrictions.eq("voter", reviewer), 
				Restrictions.eq("update", update)});
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
		save(vote);		

		pullRequestManager.refresh(request);
	}

}
