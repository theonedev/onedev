package com.pmease.gitplex.core.extensionpoint;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.model.PullRequest;

@Singleton
public class PullRequestListeners {

	private final Dao dao;
	
	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> listeners;
	
	@Inject
	public PullRequestListeners(Dao dao, UnitOfWork unitOfWork, Set<PullRequestListener> listeners) {
		this.listeners = listeners;
		this.dao = dao;
		this.unitOfWork = unitOfWork;
	}
	
	public void call(final Long requestId, final Callback callback) {
		unitOfWork.asyncCall(new Runnable() {

			@Override
			public void run() {
				PullRequest request = dao.load(PullRequest.class, requestId);
				for (PullRequestListener listener: listeners)
					callback.call(listener, request);
			}
			
		});
	}
	
	public static abstract class Callback {
		protected abstract void call(PullRequestListener listener, PullRequest request);
	}
}
