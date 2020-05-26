package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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

	@Override
	public void populateVerifications(Collection<PullRequest> requests) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestVerification> query = builder.createQuery(PullRequestVerification.class);
		
		Root<PullRequestVerification> root = query.from(PullRequestVerification.class);
		query.select(root);
		root.join(PullRequestVerification.PROP_REQUEST);
		
		query.where(root.get(PullRequestVerification.PROP_REQUEST).in(requests));
		
		for (PullRequest request: requests)
			request.setVerifications(new ArrayList<>());
		
		for (PullRequestVerification verification: getSession().createQuery(query).getResultList())
			verification.getRequest().getVerifications().add(verification);
	}

}
