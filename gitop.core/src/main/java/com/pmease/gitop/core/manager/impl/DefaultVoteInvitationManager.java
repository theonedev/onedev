package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.VoteInvitation;

@Singleton
public class DefaultVoteInvitationManager extends AbstractGenericDao<VoteInvitation> implements VoteInvitationManager {

	@Inject
	public DefaultVoteInvitationManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public VoteInvitation find(User voter, PullRequest request) {
		return find(new Criterion[]{Restrictions.eq("voter", voter), Restrictions.eq("request", request)});
	}

}
