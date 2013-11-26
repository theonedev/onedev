package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.core.model.PullRequestUpdate;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractGenericDao<PullRequestUpdate>
		implements PullRequestUpdateManager {

	@Inject
	public DefaultPullRequestUpdateManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		super.save(update);

		Git git = update.getRequest().getTarget().getProject().getCodeRepo();
		git.updateRef(update.getRefName(), update.getCommitHash(), null, null);
	}

	@Transactional
	@Override
	public void delete(PullRequestUpdate update) {
		super.delete(update);

		Git git = update.getRequest().getTarget().getProject().getCodeRepo();
		git.deleteRef(update.getRefName());
	}

}
