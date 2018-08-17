package io.onedev.server.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.build.BuildStarted;
import io.onedev.server.manager.BuildManager;
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
		criteria.addOrder(Order.desc("id"));
		List<Build> builds = findRange(criteria, 0, 1);
		return !builds.isEmpty()?builds.iterator().next():null;
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
		if (build.isNew())
			build.setNumber(getNextNumber(build.getConfiguration().getProject()));
		
		super.save(build);
		
		if (build.getStatus() == Build.Status.RUNNING) 
			listenerRegistry.post(new BuildStarted(build));
		else
			listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Override
	public Build find(Project project, long number) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("number", number));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, String term, int count) {
		List<Build> builds = new ArrayList<>();
		
		Long number = null;
		String numberStr = term;
		if (numberStr != null) {
			numberStr = numberStr.trim();
			if (numberStr.startsWith("#"))
				numberStr = numberStr.substring(1);
			if (StringUtils.isNumeric(numberStr))
				number = Long.valueOf(numberStr);
		}
		
		if (number != null) {
			Build build = find(project, number);
			if (build != null)
				builds.add(build);
			EntityCriteria<Build> criteria = newCriteria();
			criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
			criteria.add(Restrictions.and(
					Restrictions.or(Restrictions.ilike("noSpaceCommitShortMessage", "%" + term + "%"), Restrictions.ilike("numberStr", term + "%")), 
					Restrictions.ne("number", number)
				));
			criteria.addOrder(Order.desc("number"));
			builds.addAll(findRange(criteria, 0, count-builds.size()));
		} else {
			EntityCriteria<Build> criteria = newCriteria();
			criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
			if (StringUtils.isNotBlank(term)) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("noSpaceCommitShortMessage", "%" + term + "%"), 
						Restrictions.ilike("numberStr", (term.startsWith("#")? term.substring(1): term) + "%")));
			}
			criteria.addOrder(Order.desc("number"));
			builds.addAll(findRange(criteria, 0, count));
		} 
		return builds;
	}
	
}
