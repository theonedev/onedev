package io.onedev.server.entitymanager.impl;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.IterationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DefaultIterationManager extends BaseEntityManager<Iteration> implements IterationManager {

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultIterationManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@Sessional
	@Override
	public Iteration findInHierarchy(String iterationFQN) {
		String projectName = StringUtils.substringBefore(iterationFQN, ":");
		Project project = projectManager.findByPath(projectName);
		if (project != null) { 
			String iterationName = StringUtils.substringAfter(iterationFQN, ":");
			EntityCriteria<Iteration> criteria = EntityCriteria.of(Iteration.class);
			criteria.add(Restrictions.in("project", project.getSelfAndAncestors()));
			criteria.add(Restrictions.eq("name", iterationName));
			criteria.setCacheable(true);
			return find(criteria);
		} else { 
			return null;
		}
	}
	
	@Sessional
	@Override
	public Iteration findInHierarchy(Project project, String name) {
		EntityCriteria<Iteration> criteria = EntityCriteria.of(Iteration.class);
		criteria.add(Restrictions.in("project", project.getSelfAndAncestors()));
		criteria.add(Restrictions.eq("name", name));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Override
	public List<Iteration> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}

	@Transactional
	@Override
	public void createOrUpdate(Iteration iteration) {
		dao.persist(iteration);
	}
	
}
