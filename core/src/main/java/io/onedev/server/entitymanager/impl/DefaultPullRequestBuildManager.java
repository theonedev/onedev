package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.PullRequestBuildManager;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestBuildManager extends AbstractEntityManager<PullRequestBuild> 
		implements PullRequestBuildManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestBuildManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void savePullRequestBuilds(PullRequest request) {
		Collection<Long> ids = new HashSet<>();
		for (PullRequestBuild pullRequestBuild: request.getPullRequestBuilds()) {
			boolean isNew = pullRequestBuild.isNew();
			save(pullRequestBuild);
			if (isNew)
				listenerRegistry.post(new PullRequestBuildEvent(pullRequestBuild));
			ids.add(pullRequestBuild.getId());
		}
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery("delete from PullRequestBuild where request=:request and id not in (:ids)");
			query.setParameter("request", request);
			query.setParameter("ids", ids);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from PullRequestBuild where request=:request");
			query.setParameter("request", request);
			query.executeUpdate();
		}
	}

}
