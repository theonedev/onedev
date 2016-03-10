package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.StorageManager;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractEntityDao<PullRequestUpdate> implements PullRequestUpdateManager {
	
	private final StorageManager storageManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final CommentManager commentManager;
	
	private final UnitOfWork unitOfWork;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, 
			StorageManager storageManager, Set<PullRequestListener> pullRequestListeners, 
			UnitOfWork unitOfWork, CommentManager commentManager) {
		super(dao);
		
		this.storageManager = storageManager;
		this.pullRequestListeners = pullRequestListeners;
		this.unitOfWork = unitOfWork;
		this.commentManager = commentManager;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update, boolean notify) {
		persist(update);
		
		FileUtils.cleanDir(storageManager.getCacheDir(update));

		PullRequest request = update.getRequest();
		String sourceHead = request.getSource().getObjectName();

		if (!request.getTargetDepot().equals(request.getSourceDepot())) {
			request.getTargetDepot().git().fetch(
					request.getSourceDepot().git(), 
					"+" + request.getSourceRef() + ":" + update.getHeadRef()); 
		} else {
			request.getTargetDepot().git().updateRef(update.getHeadRef(), 
					sourceHead, null, null);
		}
		
		if (notify) { 
			for (PullRequestListener listener: pullRequestListeners)
				listener.onUpdated(update);
		}

		// Inline comment update can be time consuming and we do it inside a separate thread 
		// in order not to blocking the push operation
		final Long requestId = request.getId();
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						PullRequest request = dao.load(PullRequest.class, requestId);
						for (Comment comment: request.getComments()) {
							if (comment.getInlineInfo() != null)
								commentManager.updateInlineInfo(comment);
						}
					}
					
				});
			}
			
		});
	}

	@Transactional
	@Override
	public void delete(PullRequestUpdate update) {
		update.deleteRefs();
		remove(update);
	}

}
