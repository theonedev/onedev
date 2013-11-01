package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestUpdateManager;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;

@Singleton
public class DefaultMergeRequestUpdateManager extends AbstractGenericDao<MergeRequestUpdate>
		implements MergeRequestUpdateManager {

	private final StorageManager storageManager;

	@Inject
	public DefaultMergeRequestUpdateManager(GeneralDao generalDao, StorageManager storageManager) {
		super(generalDao);
		this.storageManager = storageManager;
	}

	@Transactional
	@Override
	public void save(MergeRequestUpdate update) {
		super.save(update);

		Git git =
				new Git(storageManager.getStorage(update.getRequest().getTarget().getProject())
						.ofCode());
		git.updateRef().refName(update.getRefName()).revision(update.getCommitHash()).call();
	}

	@Transactional
	@Override
	public void delete(MergeRequestUpdate update) {
		super.delete(update);

		Git git =
				new Git(storageManager.getStorage(update.getRequest().getTarget().getProject())
						.ofCode());
		git.deleteRef().refName(update.getRefName()).call();
	}

}
