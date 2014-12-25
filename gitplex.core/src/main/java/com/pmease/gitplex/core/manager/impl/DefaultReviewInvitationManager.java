package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.ReviewInvitation;

@Singleton
public class DefaultReviewInvitationManager implements ReviewInvitationManager {

	private final Dao dao;
	
	@Inject
	public DefaultReviewInvitationManager(Dao dao) {
		this.dao = dao;
	}

	@Sessional
	@Override
	public ReviewInvitation find(User reviewer, PullRequest request) {
		return dao.find(EntityCriteria.of(ReviewInvitation.class)
				.add(Restrictions.eq("reviewer", reviewer))
				.add(Restrictions.eq("request", request)));
	}

}
