package com.pmease.gitop.core.manager.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.Commit;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.gitop.core.event.BranchRefUpdateEvent;
import com.pmease.gitop.core.manager.AutoPushManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.AutoPush;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;

@Singleton
public class DefaultAutoPushManager extends AbstractGenericDao<AutoPush> 
		implements AutoPushManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAutoPushManager.class);
	
	private final PullRequestManager pullRequestManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Executor executor;
	
	@Inject
	public DefaultAutoPushManager(GeneralDao generalDao, PullRequestManager pullRequestManager, 
			UnitOfWork unitOfWork, Executor executor) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
		this.unitOfWork = unitOfWork;
		this.executor = executor;
	}

	@Sessional
	@Subscribe
	public void pushUpon(BranchRefUpdateEvent event) {
		Set<Branch> branchesWithOpenRequests = new HashSet<>();
		for (PullRequest request: event.getBranch().getOutgoingRequests()) {
			if (request.isOpen()) {
				branchesWithOpenRequests.add(request.getTarget());
			}
		}
		for (final AutoPush autoPush: event.getBranch().getAutoPushTargets()) {
			if (!branchesWithOpenRequests.contains(autoPush.getTarget())) {
				executor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							unitOfWork.call(new Callable<Void>() {
	
								@Override
								public Void call() throws Exception {
									AutoPush reloaded = load(autoPush.getId());
									Branch source = reloaded.getSource();
									Commit commit = source.getProject().code().resolveRevision(source.getHeadRef());
									PullRequest request = pullRequestManager.create(
											commit.getSubject(), reloaded.getTarget(), source, true);
									if (request != null)
										pullRequestManager.refresh(request);
									return null;
								}
								
							});
						} catch (Exception e) {
							logger.error("Error performing auto-push.", e);
							throw ExceptionUtils.unchecked(e);
						}
					}
					
				});
			}
		}
	}
}
