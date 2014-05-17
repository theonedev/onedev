package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.VoteInvitation;

@Singleton
public class DefaultVoteInvitationManager implements VoteInvitationManager {

	private final Dao dao;
	
	@Inject
	public DefaultVoteInvitationManager(Dao dao) {
		this.dao = dao;
	}

	@Sessional
	@Override
	public VoteInvitation find(User voter, PullRequest request) {
		return dao.find(EntityCriteria.of(VoteInvitation.class).add(Restrictions.eq("voter", voter)).add(Restrictions.eq("request", request)));
	}

}
