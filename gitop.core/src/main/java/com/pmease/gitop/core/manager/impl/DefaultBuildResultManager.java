package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.BuildResultManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.BuildResult;
import com.pmease.gitop.model.PullRequest;

@Singleton
public class DefaultBuildResultManager extends AbstractGenericDao<BuildResult>
		implements BuildResultManager {

	private final PullRequestManager pullRequestManager;

	@Inject
	public DefaultBuildResultManager(GeneralDao generalDao,
			PullRequestManager pullRequestManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
	}

	@Sessional
	@Override
	public Collection<BuildResult> findBy(String commit) {
		return query(Restrictions.eq("commit", commit));
	}

	@Sessional
	@Override
	public BuildResult findBy(String commit, String configuration) {
		return find(Restrictions.eq("commit", commit),
				Restrictions.eq("configuration", configuration));
	}

	@Override
	public void save(BuildResult result) {
		super.save(result);

		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}

	}

	@Override
	public void delete(BuildResult result) {
		super.delete(result);
		
		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}
	}

}
