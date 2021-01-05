package io.onedev.server.entitymanager.impl;

import java.util.Date;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Objects;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDescriptionChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeStrategyChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestTitleChangeData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestChangeManager extends BaseEntityManager<PullRequestChange> 
		implements PullRequestChangeManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestChangeManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestChange change) {
		dao.persist(change);
		listenerRegistry.post(new PullRequestChangeEvent(change));
	}
	
	@Transactional
	@Override
	public void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy) {
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestMergeStrategyChangeData(request.getMergeStrategy(), mergeStrategy));
		change.setUser(SecurityUtils.getUser());
		save(change);
		request.setMergeStrategy(mergeStrategy);
	}

	@Transactional
	@Override
	public void changeTitle(PullRequest request, String title) {
		String prevTitle = request.getTitle();
		if (!title.equals(prevTitle)) {
			request.setTitle(title);
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setRequest(request);
			change.setData(new PullRequestTitleChangeData(prevTitle, title));
			change.setUser(SecurityUtils.getUser());
			save(change);
		}
	}

	@Transactional
	@Override
	public void changeDescription(PullRequest request, @Nullable String description) {
		String prevDescription = request.getDescription();
		if (!Objects.equal(prevDescription, description)) {
			request.setDescription(description);
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setRequest(request);
			change.setData(new PullRequestDescriptionChangeData(prevDescription, description));
			change.setUser(SecurityUtils.getUser());
			save(change);
		}
	}
	
}
