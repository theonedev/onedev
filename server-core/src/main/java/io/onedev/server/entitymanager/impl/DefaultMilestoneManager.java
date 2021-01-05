package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultMilestoneManager extends BaseEntityManager<Milestone> implements MilestoneManager {

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultMilestoneManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@Sessional
	@Override
	public Milestone find(String milestoneFQN) {
		String projectName = StringUtils.substringBefore(milestoneFQN, ":");
		Project project = projectManager.find(projectName);
		if (project != null) 
			return find(project, StringUtils.substringAfter(milestoneFQN, ":"));
		else 
			return null;
	}
	
	@Transactional
	@Override
	public void delete(Milestone milestone) {
		Query<?> query = getSession().createQuery("update Issue set milestone=null where milestone=:milestone");
		query.setParameter("milestone", milestone);
		query.executeUpdate();
		super.delete(milestone);
	}
	
	@Sessional
	@Override
	public Milestone find(Project project, String name) {
		EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.ilike("name", name));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Override
	public List<Milestone> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
