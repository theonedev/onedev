package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.support.LastEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCommented;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractEntityManager<PullRequestComment> 
		implements PullRequestCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, PullRequestManager pullRequestManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);

		this.listenerRegistry = listenerRegistry;
		this.pullRequestManager = pullRequestManager;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		save(comment, true);
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment, boolean notifyListeners) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (notifyListeners && isNew) {
			PullRequestCommented event = new PullRequestCommented(comment);
			listenerRegistry.post(event);
			
			LastEvent lastEvent = new LastEvent();
			lastEvent.setDate(event.getDate());
			lastEvent.setType(EditableUtils.getName(event.getClass()));
			lastEvent.setUser(event.getUser());
			comment.getRequest().setLastEvent(lastEvent);
			pullRequestManager.save(event.getRequest());
		}
	}

}
