package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.extensionpoint.PullRequestListeners;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;

@Singleton
public class DefaultPullRequestUpdateManager implements PullRequestUpdateManager {
	
	private final Dao dao;
	
	private final StorageManager storageManager;
	
	private final PullRequestListeners pullRequestListeners;
	
	private final UnitOfWork unitOfWork;
	
	private final PullRequestCommentManager pullRequestCommentManager;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, StorageManager storageManager, 
			PullRequestListeners pullRequestListeners, UnitOfWork unitOfWork, 
			PullRequestCommentManager pullRequestCommentManager) {
		this.dao = dao;
		this.storageManager = storageManager;
		this.pullRequestListeners = pullRequestListeners;
		this.unitOfWork = unitOfWork;
		this.pullRequestCommentManager = pullRequestCommentManager;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		dao.persist(update);
		
		FileUtils.cleanDir(storageManager.getCacheDir(update));

		PullRequest request = update.getRequest();
		String sourceHead = request.getSource().getHeadCommitHash();

		if (!request.getTarget().getRepository().equals(request.getSource().getRepository())) {
			request.getTarget().getRepository().git().fetch(
					request.getSource().getRepository().git(), 
					"+" + request.getSource().getHeadRef() + ":" + update.getHeadRef()); 
		} else {
			request.getTarget().getRepository().git().updateRef(update.getHeadRef(), 
					sourceHead, null, null);
		}
		
		final Long requestId = request.getId();
		
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						PullRequest request = dao.load(PullRequest.class, requestId);
						for (PullRequestComment comment: request.getComments()) {
							if (comment.getInlineInfo() != null)
								pullRequestCommentManager.updateInline(comment);
						}

						pullRequestListeners.call(request, new PullRequestListeners.Callback() {
							
							@Override
							protected void call(PullRequestListener listener, PullRequest request) {
								listener.onUpdated(request);
							}
							
						});
					}
					
				});
			}
			
		});
		
	}

	@Transactional
	@Override
	public void delete(PullRequestUpdate update) {
		update.deleteRefs();
		dao.remove(update);
	}

}
