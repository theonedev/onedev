package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestUpdateManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;

@Singleton
public class DefaultMergeRequestUpdateManager extends AbstractGenericDao<MergeRequestUpdate>
		implements MergeRequestUpdateManager {

	@Inject
	public DefaultMergeRequestUpdateManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Transactional
	@Override
	public void save(MergeRequestUpdate update) {
		super.save(update);

		Git git = update.getRequest().getTarget().getProject().getCodeRepo();
		git.updateRef(update.getRefName(), update.getCommitHash(), null, null);
	}

	@Transactional
	@Override
	public void delete(MergeRequestUpdate update) {
		super.delete(update);

		Git git = update.getRequest().getTarget().getProject().getCodeRepo();
		git.deleteRef(update.getRefName());
	}

}
