package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.manager.CommitVerificationManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.CommitVerification;
import com.pmease.gitop.model.PullRequest;

@Singleton
public class DefaultCommitVerificationManager implements CommitVerificationManager {

	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;

	@Inject
	public DefaultCommitVerificationManager(Dao dao, PullRequestManager pullRequestManager) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
	}

	@Sessional
	@Override
	public Collection<CommitVerification> findBy(Branch branch, String commit) {
		return dao.query(EntityCriteria.of(CommitVerification.class)
				.add(Restrictions.eq("branch", branch))
				.add(Restrictions.eq("commit", commit)), 0, 0);
	}

	@Sessional
	@Override
	public CommitVerification findBy(Branch branch, String commit, String configuration) {
		return dao.find(EntityCriteria.of(CommitVerification.class)
				.add(Restrictions.eq("branch", branch))
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("configuration", configuration)));
	}

	@Override
	public void save(CommitVerification result) {
		dao.persist(result);

		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}

	}

	@Override
	public void delete(CommitVerification result) {
		dao.remove(result);
		
		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}
	}

}
