package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.Group;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultGroupManager extends AbstractEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@Transactional
	@Override
	public void save(Group group, String oldName) {
		if (oldName != null && !oldName.equals(group.getName())) {
			for (Project project: projectManager.findAll()) {
				for (BranchProtection protection: project.getBranchProtections()) 
					protection.onGroupRename(project, oldName, group.getName());
				for (TagProtection protection: project.getTagProtections())
					protection.onGroupRename(oldName, group.getName());
			}
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
		for (Project project: projectManager.findAll()) {
			for (BranchProtection protection: project.getBranchProtections()) 
				protection.onGroupDelete(project, group.getName());
			for (TagProtection protection: project.getTagProtections())
				protection.onGroupDelete(group.getName());
		}
		dao.remove(group);
	}

	@Sessional
	@Override
	public Group find(String name) {
		EntityCriteria<Group> criteria = newCriteria();
		criteria.add(Restrictions.eq("name", name));
		return find(criteria);
	}
	
}
