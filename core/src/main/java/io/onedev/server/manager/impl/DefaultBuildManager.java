package io.onedev.server.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.build.BuildStarted;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBuildManager extends AbstractEntityManager<Build> implements BuildManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultBuildManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
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
	public Build findPrevious(Build build) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.lt("id", build.getId()));
		criteria.add(Restrictions.eq("configuration", build.getConfiguration()));
		if (build.getRef() != null)
			criteria.add(Restrictions.eq("ref", build.getRef()));
		else
			criteria.add(Restrictions.isNull("ref"));
		criteria.addOrder(Order.desc("id"));
		List<Build> builds = query(criteria, 0, 1);
		return !builds.isEmpty()?builds.iterator().next():null;
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, String commit) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("commit", commit));
		criteria.addOrder(Order.asc("id"));
		return query(criteria);
	}

	@Override
	public Build findByCommit(Configuration configuration, String commit) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("configuration", configuration));
		criteria.add(Restrictions.eq("commit", commit));
		return find(criteria);
	}

	@Override
	public void save(Build build) {
		super.save(build);
		
		if (build.getStatus() == Build.Status.RUNNING) 
			listenerRegistry.post(new BuildStarted(build));
		else
			listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Override
	public Build findByName(Configuration configuration, String name) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("configuration", configuration));
		criteria.add(Restrictions.eq("name", name));
		criteria.addOrder(Order.desc("id"));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, String term, int count) {
		List<Build> builds = new ArrayList<>();
		
		EntityCriteria<Build> criteria = newCriteria();
		Criteria configurationCriteria = criteria.createCriteria("configuration");
		configurationCriteria.add(Restrictions.eq("project", project));
		if (term != null) {
			if (term.contains(Build.FQN_SEPARATOR)) {
				String configurationTerm = StringUtils.substringBefore(term, Build.FQN_SEPARATOR);
				String nameTerm = StringUtils.substringAfter(term, Build.FQN_SEPARATOR);
				configurationCriteria.add(Restrictions.ilike("name", "%" + configurationTerm + "%"));
				criteria.add(Restrictions.ilike("name", "%" + nameTerm + "%"));
			} else {
				criteria.add(Restrictions.ilike("name", "%" + term + "%"));
			}
		}
		criteria.addOrder(Order.desc("id"));
		builds.addAll(query(criteria, 0, count-builds.size()));
		return builds;
	}

	@Override
	public List<Build> queryAfter(Project project, Long afterBuildId, int count) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
		criteria.addOrder(Order.asc("id"));
		if (afterBuildId != null)
			criteria.add(Restrictions.gt("id", afterBuildId));
		return query(criteria, 0, count);
	}

	@Sessional
	@Override
	public Build findByFQN(Project project, String fqn) {
		String configurationName = StringUtils.substringBefore(fqn, Build.FQN_SEPARATOR);
		String buildName = StringUtils.substringAfter(fqn, Build.FQN_SEPARATOR);
		Configuration configuration = OneDev.getInstance(ConfigurationManager.class).find(project, configurationName);
		if (configuration != null)
			return OneDev.getInstance(BuildManager.class).findByName(configuration, buildName);
		else
			return null;
	}

}
