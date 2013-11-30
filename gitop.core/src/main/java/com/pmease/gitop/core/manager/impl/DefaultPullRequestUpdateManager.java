package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.PullRequestUpdate;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractGenericDao<PullRequestUpdate>
		implements PullRequestUpdateManager {

	@Inject
	public DefaultPullRequestUpdateManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate entity) {
		super.save(entity);

		Git git = entity.getRequest().getTarget().getProject().code();
		git.updateRef(entity.getHeadRef(), entity.getHeadCommit(), null, null);
	}

	@Transactional
	@Override
	public void delete(PullRequestUpdate entity) {
		super.delete(entity);

		Git git = entity.getRequest().getTarget().getProject().code();
		git.deleteRef(entity.getBaseRef());
		git.deleteRef(entity.getHeadRef());
	}

}
