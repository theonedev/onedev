package com.pmease.gitop.core.manager.impl;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.manager.PendingVoteManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.PendingVote;
import com.pmease.gitop.core.model.User;

@Singleton
public class DefaultPendingVoteManager extends DefaultGenericDao<PendingVote> implements PendingVoteManager {

	public DefaultPendingVoteManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Transactional
	@Override
	public PendingVote find(User reviewer, MergeRequest request) {
		return find(new Criterion[]{Restrictions.eq("reviewer", reviewer), Restrictions.eq("request", request)});
	}

	@Transactional
	@Override
	public void save(PendingVote pendingVote) {
		if (pendingVote.getId() == null) {
			pendingVote.getRequest().getPendingVotes().add(pendingVote);
			pendingVote.getReviewer().getPendingVotes().add(pendingVote);
		}
		super.save(pendingVote);
	}

}
