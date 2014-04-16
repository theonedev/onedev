package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractGenericDao<PullRequestUpdate>
		implements PullRequestUpdateManager {
	
	@Inject
	public DefaultPullRequestUpdateManager(GeneralDao generalDao, 
			PullRequestManager pullRequestManager) {
		super(generalDao);
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		super.save(update);

		Git git = update.getRequest().getTarget().getRepository().git();
		git.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
	}

	@Transactional
	@Override
	public void delete(PullRequestUpdate update) {
		update.deleteRefs();
		super.delete(update);
	}

	@Transactional
	@Override
	public void realize(PullRequestUpdate update) {
		save(update);
		
		PullRequest request = update.getRequest();
		String sourceHead = request.getSource().getHeadCommit();

		if (!request.getTarget().getRepository().equals(request.getSource().getRepository())) {
			request.getTarget().getRepository().git().fetch(
					request.getSource().getRepository().git().repoDir().getAbsolutePath(), 
					"+" + sourceHead + ":" + update.getHeadRef()); 
		} else {
			request.getTarget().getRepository().git().updateRef(update.getHeadRef(), 
					sourceHead, null, null);
		}
	}

}
