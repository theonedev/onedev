package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.PullRequest;
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
	public void save(PullRequestUpdate update) {
		super.save(update);

		Git git = update.getRequest().getTarget().getProject().code();
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
	public void update(PullRequest request) {
		Git sourceGit = request.getSource().getProject().code();
		String sourceHead = sourceGit.resolveRef(request.getSource().getHeadRef(), true);
		Git targetGit = request.getTarget().getProject().code();
		String targetHead = targetGit.resolveRef(request.getTarget().getHeadRef(), true);
		
		if (!targetGit.isAncestor(sourceHead, targetHead)) {
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			request.getUpdates().add(update);
			update.setHeadCommit(sourceHead);
			save(update);
			
			targetGit.fetch(sourceGit.repoDir().getAbsolutePath(), "+" + sourceHead + ":" + update.getHeadRef()); 
		}
	}

}
