package io.onedev.server.manager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBuildManager extends AbstractEntityManager<Build> implements BuildManager {

	private final PullRequestManager pullRequestManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultBuildManager(Dao dao, PullRequestManager pullRequestManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.pullRequestManager = pullRequestManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void delete(Build build) {
    	Query<?> query = getSession().createQuery("update PullRequestBuild set build=null where build=:build");
    	query.setParameter("build", build);
    	query.executeUpdate();
    	
    	super.delete(build);
	}

	@Sessional
	@Override
	public List<Build> findAll(Project project, String commit) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("commit", commit));
		criteria.addOrder(Order.asc("id"));
		return findAll(criteria);
	}

	@Override
	public Build find(Configuration configuration, String commit) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("configuration", configuration));
		criteria.add(Restrictions.eq("commit", commit));
		return find(criteria);
	}

	@Override
	public void save(Build build) {
		super.save(build);
		
		for (PullRequest request: pullRequestManager.findOpenByCommit(build.getCommit())) {
			PullRequestBuildEvent event = new PullRequestBuildEvent(request, build);
			listenerRegistry.post(event);
		}		
	}
	
}
