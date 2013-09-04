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
	public PendingVote lookupPendingVote(User reviewer, MergeRequest request) {
		return lookup(new Criterion[]{Restrictions.eq("reviewer", reviewer), Restrictions.eq("request", request)});
	}

	@Transactional
	@Override
	public PendingVote requestVote(User reviewer, MergeRequest request) {
		PendingVote pendingVote = lookupPendingVote(reviewer, request);
		if (pendingVote == null) {
			pendingVote = new PendingVote();
			pendingVote.setRequest(request);
			pendingVote.setReviewer(reviewer);
			save(pendingVote);
		}
		return pendingVote;
	}

}
