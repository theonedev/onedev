package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.util.ThreadContext;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.TransactionInterceptor;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.entity.Verification.Status;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.VerificationManager;

@Singleton
public class DefaultVerificationManager extends AbstractEntityManager<Verification> implements VerificationManager {

	private final PullRequestManager pullRequestManager;
	
	private final AccountManager accountManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Provider<Set<PullRequestListener>> listenersProvider;

	@Inject
	public DefaultVerificationManager(Dao dao, AccountManager accountManager, 
			PullRequestManager pullRequestManager, UnitOfWork unitOfWork, 
			Provider<Set<PullRequestListener>> listenersProvider) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.accountManager = accountManager;
		this.unitOfWork = unitOfWork;
		this.listenersProvider = listenersProvider;
	}

	@Sessional
	@Override
	public Collection<Verification> findAll(PullRequest request, String commit) {
		return findRange(EntityCriteria.of(Verification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit)), 0, 0);
	}

	@Sessional
	@Override
	public Verification find(PullRequest request, String commit, String configuration) {
		return find(EntityCriteria.of(Verification.class)
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("configuration", configuration)));
	}

	@Transactional
	@Override
	public void save(Verification verification) {
		dao.persist(verification);

		PullRequest request = verification.getRequest();
		requestToCheck(request);

		if (TransactionInterceptor.isInitiating()) {
			for (PullRequestListener listener: listenersProvider.get()) {
				listener.onVerifyRequest(verification.getRequest());
			}
		}
	}

	@Transactional
	@Override
	public void delete(Verification verification) {
		dao.remove(verification);
		
		requestToCheck(verification.getRequest());
	}

	@Sessional
	private void requestToCheck(PullRequest request) {
		Long requestId = request.getId();
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						try {
					        ThreadContext.bind(accountManager.getRoot().asSubject());
							pullRequestManager.check(dao.load(PullRequest.class, requestId));
						} finally {
							ThreadContext.unbindSubject();
						}
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
