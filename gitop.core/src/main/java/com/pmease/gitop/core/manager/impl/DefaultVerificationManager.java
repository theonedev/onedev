package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestVerificationManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Verification;

@Singleton
public class DefaultVerificationManager implements PullRequestVerificationManager {

	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;

	@Inject
	public DefaultVerificationManager(Dao dao, PullRequestManager pullRequestManager) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
	}

	@Sessional
	@Override
	public Collection<Verification> findBy(PullRequest request, String commit) {
		return dao.query(EntityCriteria.of(Verification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit)), 0, 0);
	}

	@Sessional
	@Override
	public Verification findBy(PullRequest request, String commit, String configuration) {
		return dao.find(EntityCriteria.of(Verification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("configuration", configuration)));
	}

	@Override
	public void save(Verification result) {
		dao.persist(result);

		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}

	}

	@Override
	public void delete(Verification result) {
		dao.remove(result);
		
		for (PullRequest request : pullRequestManager.findByCommit(result.getCommit())) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}
	}

}
