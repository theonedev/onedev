package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.PullRequestVerificationManager;
import io.onedev.server.event.pullrequest.PullRequestVerificationEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestVerification;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestVerificationManager extends AbstractEntityManager<PullRequestVerification> 
		implements PullRequestVerificationManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestVerificationManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void saveVerifications(PullRequest request) {
		Collection<Build> builds = request.getVerifications().stream()
				.filter(it->!it.isNew()).map(it->it.getBuild()).collect(Collectors.toList());
		if (!builds.isEmpty()) {
			Query query = getSession().createQuery("delete from PullRequestVerification where request=:request and build not in (:builds)");
			query.setParameter("request", request);
			query.setParameter("builds", builds);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from PullRequestVerification where request=:request");
			query.setParameter("request", request);
			query.executeUpdate();
		}
		for (PullRequestVerification build: request.getVerifications()) {
			boolean isNew = build.isNew();
			save(build);
			if (isNew)
				listenerRegistry.post(new PullRequestVerificationEvent(build));
		}
	}

}
