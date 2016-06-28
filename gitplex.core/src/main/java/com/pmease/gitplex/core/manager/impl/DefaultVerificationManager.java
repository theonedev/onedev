package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.entity.Verification.Status;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.VerificationManager;

@Singleton
public class DefaultVerificationManager extends AbstractEntityManager<Verification> implements VerificationManager {

	private final PullRequestManager pullRequestManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> pullRequestListeners;

	@Inject
	public DefaultVerificationManager(Dao dao, 
			PullRequestManager pullRequestManager, UnitOfWork unitOfWork, 
			Set<PullRequestListener> pullRequestListeners) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public Collection<Verification> findBy(PullRequest request, String commit) {
		return query(EntityCriteria.of(Verification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit)), 0, 0);
	}

	@Sessional
	@Override
	public Verification findBy(PullRequest request, String commit, String configuration) {
		return find(EntityCriteria.of(Verification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("configuration", configuration)));
	}

	@Transactional
	@Override
	public void save(Verification verification) {
		dao.persist(verification);

		onVerificationChange(verification.getRequest());
	}

	@Transactional
	@Override
	public void delete(Verification verification) {
		dao.remove(verification);
		
		onVerificationChange(verification.getRequest());
	}
	
	private void onVerificationChange(PullRequest request) {
		for (PullRequestListener listener: pullRequestListeners)
			listener.onVerifyRequest(request);

		final Long requestId = request.getId();
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						pullRequestManager.check(dao.load(PullRequest.class, requestId));
					}
					
				});
			}
			
		});
	}

	@Override
	public Status getOverallStatus(Collection<Verification> verifications) {
		Verification.Status overallStatus = null;
		for (Verification verification: verifications) {
			if (verification.getStatus() == Verification.Status.NOT_PASSED) {
				overallStatus = Verification.Status.NOT_PASSED;
				break;
			} else if (verification.getStatus() == Verification.Status.ONGOING) {
				overallStatus = Verification.Status.ONGOING;
			} else if (overallStatus == null) {
				overallStatus = Verification.Status.PASSED;
			}
		}
		return overallStatus;
	}

}
