package io.onedev.server.service.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.ProjectService;

@Singleton
public class DefaultIterationService extends BaseEntityService<Iteration> implements IterationService {

	@Inject
	private ProjectService projectService;

	@Sessional
	@Override
	public Iteration findInHierarchy(String iterationFQN) {
		String projectName = StringUtils.substringBefore(iterationFQN, ":");
		Project project = projectService.findByPath(projectName);
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
