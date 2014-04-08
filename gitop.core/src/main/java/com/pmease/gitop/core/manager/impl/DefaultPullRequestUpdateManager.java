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
import com.pmease.gitop.model.User;

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

		Git git = update.getRequest().getTarget().getProject().git();
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
	public void update(PullRequest request, User user) {
		String sourceHead = request.getSource().getHeadCommit();
		
		PullRequestUpdate update = new PullRequestUpdate();
		update.setRequest(request);
		update.setUser(user);
		request.getUpdates().add(update);
		update.setHeadCommit(sourceHead);
		request.setUpdateDate(new Date());
		save(update);
		pullRequestManager.save(request);
		
		if (!request.getTarget().getProject().equals(request.getSource().getProject())) {
			request.getTarget().getProject().git().fetch(
					request.getSource().getProject().git().repoDir().getAbsolutePath(), 
					"+" + sourceHead + ":" + update.getHeadRef()); 
		} else {
			request.getTarget().getProject().git().updateRef(update.getHeadRef(), 
					sourceHead, null, null);
		}
	}

}
