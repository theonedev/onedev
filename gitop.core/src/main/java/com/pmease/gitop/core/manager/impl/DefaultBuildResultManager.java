package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.manager.BuildResultManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.BuildResult;
import com.pmease.gitop.model.PullRequest;

@Singleton
public class DefaultBuildResultManager implements BuildResultManager {

	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;

	@Inject
	public DefaultBuildResultManager(Dao dao,
			PullRequestManager pullRequestManager) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
	}

	@Sessional
	@Override
	public Collection<BuildResult> findBy(String commit) {
		return dao.query(EntityCriteria.of(BuildResult.class).add(Restrictions.eq("commit", commit)), 0, 0);
	}

	@Sessional
	@Override
	public BuildResult findBy(String commit, String configuration) {
		return dao.find(EntityCriteria.of(BuildResult.class)
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("configuration", configuration)));
	}

	@Override
	public void save(BuildResult result) {
		dao.persist(result);

		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}

	}

	@Override
	public void delete(BuildResult result) {
		dao.remove(result);
		
		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}
	}

}
