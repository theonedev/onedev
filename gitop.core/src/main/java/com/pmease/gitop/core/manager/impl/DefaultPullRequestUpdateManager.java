package com.pmease.gitop.core.manager.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.gitop.core.event.BranchRefUpdateEvent;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractGenericDao<PullRequestUpdate>
		implements PullRequestUpdateManager {

	private final Logger logger = LoggerFactory.getLogger(DefaultPullRequestUpdateManager.class);
	
	private final PullRequestManager pullRequestManager;
	
	private final Executor executor;
	
	private final UnitOfWork unitOfWork;
	
	@Inject
	public DefaultPullRequestUpdateManager(GeneralDao generalDao, PullRequestManager pullRequestManager,
			Executor executor, UnitOfWork unitOfWork) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
		this.executor = executor;
		this.unitOfWork = unitOfWork;
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
		Git sourceGit = request.getSource().getProject().code();
		String sourceHead = sourceGit.resolveRef(request.getSource().getHeadRef(), true);
		Git targetGit = request.getTarget().getProject().code();
		String targetHead = targetGit.resolveRef(request.getTarget().getHeadRef(), true);
		
		if (!targetGit.isAncestor(sourceHead, targetHead)) {
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			request.getUpdates().add(update);
			update.setHeadCommit(sourceHead);
			save(update);
			
			targetGit.fetch(sourceGit.repoDir().getAbsolutePath(), "+" + sourceHead + ":" + update.getHeadRef()); 
		}
	}

	@Sessional
	@Subscribe
	public void updateUpon(BranchRefUpdateEvent event) {
		// Calculates most recent open pull request for each branch. Note that although 
		// we do not allow multiple opened requests for a single branch, there still 
		// exist some chances multiple requests are opening for same branch, so we need 
		// to handle this case here.
		Map<Branch, PullRequest> branchRequests = new HashMap<>();
		for (PullRequest request: event.getBranch().getOutgoingRequests()) {
			if (request.isOpen()) {
				PullRequest branchRequest = branchRequests.get(request.getTarget());
				if (branchRequest == null)
					branchRequests.put(request.getTarget(), request);
				else if (request.getId() > branchRequest.getId())
					branchRequests.put(request.getTarget(), request);
			}
		}
		
		for (final PullRequest request: branchRequests.values()) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						unitOfWork.call(new Callable<Void>() {
	
							@Override
							public Void call() throws Exception {
									// Reload request to avoid Hibernate LazyInitializationException
									update(pullRequestManager.load(request.getId()));
									return null;
							}
							
						});
					} catch (Exception e) {
						logger.error("Error updating pull request.", e);
						throw ExceptionUtils.unchecked(e);
					}
				}
				
			});
		}
		
	}
	
}
