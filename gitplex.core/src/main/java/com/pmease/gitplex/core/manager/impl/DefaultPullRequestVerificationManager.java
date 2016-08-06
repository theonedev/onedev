package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestVerification;
import com.pmease.gitplex.core.entity.PullRequestVerification.Status;
import com.pmease.gitplex.core.event.pullrequest.PullRequestVerificationDeleted;
import com.pmease.gitplex.core.event.pullrequest.PullRequestVerificationFailed;
import com.pmease.gitplex.core.event.pullrequest.PullRequestVerificationRunning;
import com.pmease.gitplex.core.event.pullrequest.PullRequestVerificationSucceeded;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestVerificationManager;

@Singleton
public class DefaultPullRequestVerificationManager extends AbstractEntityManager<PullRequestVerification> 
		implements PullRequestVerificationManager {

	private final AccountManager accountManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestVerificationManager(Dao dao, AccountManager accountManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.accountManager = accountManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestVerification entity) {
		super.save(entity);
		
		if (entity.getStatus() == PullRequestVerification.Status.FAILED)
			listenerRegistry.notify(new PullRequestVerificationFailed(entity));
		else if (entity.getStatus() == PullRequestVerification.Status.RUNNING)
			listenerRegistry.notify(new PullRequestVerificationRunning(entity));
		else
			listenerRegistry.notify(new PullRequestVerificationSucceeded(entity));
	}

	@Transactional
	@Override
	public void delete(PullRequestVerification entity) {
		super.delete(entity);
		listenerRegistry.notify(new PullRequestVerificationDeleted(entity, accountManager.getCurrent()));
	}

	@Sessional
	@Override
	public Collection<PullRequestVerification> findAll(PullRequest request, String commit) {
		return findRange(EntityCriteria.of(PullRequestVerification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit)), 0, 0);
	}

	@Sessional
	@Override
	public PullRequestVerification find(PullRequest request, String commit, String configuration) {
		return find(EntityCriteria.of(PullRequestVerification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("configuration", configuration)));
	}

	@Override
	public Status getOverallStatus(Collection<PullRequestVerification> verifications) {
		PullRequestVerification.Status overallStatus = null;
		for (PullRequestVerification verification: verifications) {
			if (verification.getStatus() == PullRequestVerification.Status.FAILED) {
				overallStatus = PullRequestVerification.Status.FAILED;
				break;
			} else if (verification.getStatus() == PullRequestVerification.Status.RUNNING) {
				overallStatus = PullRequestVerification.Status.RUNNING;
			} else if (overallStatus == null) {
				overallStatus = PullRequestVerification.Status.SUCCESSFUL;
			}
		}
		return overallStatus;
	}

}
