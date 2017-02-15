package com.gitplex.server.manager.impl;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestStatusChange;
import com.gitplex.server.entity.PullRequestVerification;
import com.gitplex.server.entity.PullRequestStatusChange.Type;
import com.gitplex.server.entity.PullRequestVerification.Status;
import com.gitplex.server.event.pullrequest.PullRequestVerificationRunning;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.manager.PullRequestVerificationManager;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestVerificationManager extends AbstractEntityManager<PullRequestVerification> 
		implements PullRequestVerificationManager {

	private final AccountManager accountManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestVerificationManager(Dao dao, AccountManager accountManager, 
			PullRequestStatusChangeManager pullRequestStatusChangeManager, 
			PullRequestManager pullRequestManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.accountManager = accountManager;
		this.pullRequestStatusChangeManager = pullRequestStatusChangeManager;
		this.pullRequestManager = pullRequestManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestVerification entity) {
		super.save(entity);
		
		PullRequest request = entity.getRequest();
		
		if (entity.getStatus() == PullRequestVerification.Status.RUNNING) {
			listenerRegistry.post(new PullRequestVerificationRunning(entity));
		} else {
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setNote("configuration: " + entity.getConfiguration() + "\n"
					+ "message: " + entity.getMessage());
			statusChange.setRequest(request);
			if (entity.getStatus() == PullRequestVerification.Status.FAILED)
				statusChange.setType(Type.VERIFICATION_FAILED);
			else
				statusChange.setType(Type.VERIFICATION_SUCCEEDED);
			statusChange.setRequest(request);
			statusChange.setUser(entity.getUser());
			pullRequestStatusChangeManager.save(statusChange);
			
			request.setLastEvent(statusChange);
			pullRequestManager.save(request);
		}
	}

	@Transactional
	@Override
	public void delete(PullRequestVerification entity) {
		super.delete(entity);
		
		PullRequest request = entity.getRequest();
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setNote("configuration: " + entity.getConfiguration());
		statusChange.setRequest(request);
		statusChange.setType(Type.VERIFICATION_DELETED);
		statusChange.setUser(accountManager.getCurrent());
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
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
