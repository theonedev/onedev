package com.pmease.gitop.core.manager.impl;

import java.util.Date;

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

	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultPullRequestUpdateManager(GeneralDao generalDao, 
			PullRequestManager pullRequestManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
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
		String sourceHead = request.getSource().getHeadCommit();
		
		PullRequestUpdate update = new PullRequestUpdate();
		update.setRequest(request);
		request.getUpdates().add(update);
		update.setHeadCommit(sourceHead);
		request.setUpdateDate(new Date());
		save(update);
		pullRequestManager.save(request);
		
		if (!request.getTarget().getProject().equals(request.getSource().getProject())) {
			request.getTarget().getProject().code().fetch(
					request.getSource().getProject().code().repoDir().getAbsolutePath(), 
					"+" + sourceHead + ":" + update.getHeadRef()); 
		} else {
			request.getTarget().getProject().code().updateRef(update.getHeadRef(), 
					sourceHead, null, null);
		}
	}

}
