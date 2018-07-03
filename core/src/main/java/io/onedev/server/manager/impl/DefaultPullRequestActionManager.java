package io.onedev.server.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestActionEvent;
import io.onedev.server.manager.PullRequestActionManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.actiondata.ChangedMergeStrategyData;
import io.onedev.server.model.support.pullrequest.actiondata.ChangedTitleData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestActionManager extends AbstractEntityManager<PullRequestAction> 
		implements PullRequestActionManager {

	private final UserManager userManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestActionManager(Dao dao, UserManager userManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.userManager = userManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestAction action) {
		dao.persist(action);
		listenerRegistry.post(new PullRequestActionEvent(action));
	}
	
	@Transactional
	@Override
	public void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy) {
		PullRequestAction action = new PullRequestAction();
		action.setDate(new Date());
		action.setRequest(request);
		action.setData(new ChangedMergeStrategyData(request.getMergeStrategy(), mergeStrategy));
		action.setUser(userManager.getCurrent());
		save(action);
		request.setMergeStrategy(mergeStrategy);
	}

	@Transactional
	@Override
	public void changeTitle(PullRequest request, String title) {
		PullRequestAction action = new PullRequestAction();
		action.setDate(new Date());
		action.setRequest(request);
		action.setData(new ChangedTitleData(request.getTitle(), title));
		action.setUser(userManager.getCurrent());
		save(action);
		request.setTitle(title);
	}

}
