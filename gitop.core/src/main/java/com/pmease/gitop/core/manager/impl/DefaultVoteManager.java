package com.pmease.gitop.core.manager.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@Singleton
public class DefaultVoteManager extends DefaultGenericDao<Vote> implements VoteManager {

	public DefaultVoteManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Transactional
	@Override
	public Vote find(User reviewer, MergeRequestUpdate update) {
		return find(new Criterion[]{Restrictions.eq("reviewer", reviewer), Restrictions.eq("update", update)});
	}

}
