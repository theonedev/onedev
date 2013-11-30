package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;

@Singleton
public class DefaultVoteManager extends AbstractGenericDao<Vote> implements VoteManager {

	@Inject
	public DefaultVoteManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public Vote find(User reviewer, PullRequestUpdate update) {
		return find(new Criterion[]{Restrictions.eq("reviewer", reviewer), Restrictions.eq("update", update)});
	}

}
