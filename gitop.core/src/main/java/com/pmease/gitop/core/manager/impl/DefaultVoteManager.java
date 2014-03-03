package com.pmease.gitop.core.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestCommentManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestComment;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.Vote.Result;

@Singleton
public class DefaultVoteManager extends AbstractGenericDao<Vote> implements VoteManager {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestCommentManager pullRequestCommentManager;
	
	@Inject
	public DefaultVoteManager(GeneralDao generalDao, PullRequestManager pullRequestManager, 
			PullRequestCommentManager pullRequestCommentManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
		this.pullRequestCommentManager = pullRequestCommentManager;
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
		vote.getVoter().getVotes().add(vote);
		vote.getUpdate().getVotes().add(vote);
		save(vote);		
		
		if (StringUtils.isNotBlank(comment)) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setRequest(request);
			requestComment.setUser(user);
			requestComment.setDate(new Date());
			requestComment.setContent(comment);
			
			pullRequestCommentManager.save(requestComment);
		}
		pullRequestManager.refresh(request);
	}

}
