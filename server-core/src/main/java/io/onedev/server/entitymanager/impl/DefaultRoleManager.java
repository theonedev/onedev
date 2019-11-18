package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.Usage;

@Singleton
public class DefaultRoleManager extends AbstractEntityManager<Role> implements RoleManager {

	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	@Inject
	public DefaultRoleManager(Dao dao, SettingManager settingManager, ProjectManager projectManager) {
		super(dao);
		this.settingManager = settingManager;
		this.projectManager = projectManager;
	}

	@Transactional
	@Override
	public void save(Role role, String oldName) {
		if (oldName != null && !oldName.equals(role.getName())) { 
			for (Project project: projectManager.query())
				project.getIssueSetting().onRenameRole(oldName, role.getName());
			settingManager.getIssueSetting().onRenameRole(oldName, role.getName());
		}
		dao.persist(role);
	}

	@Transactional
	@Override
	public void delete(Role role) {
    	Usage usage = new Usage();
		for (Project project: projectManager.query()) {
			usage.add(project.getIssueSetting().onDeleteRole(role.getName()));
			usage.prefix("project '" + project.getName() + "': setting");
		}

		usage.add(settingManager.getIssueSetting().onDeleteRole(role.getName()).prefix("administration"));

		usage.checkInUse("Role '" + role.getName() + "'");
    	
		dao.remove(role);
	}

	@Override
	public List<Role> query() {
		return query(true);
	}
	
	@Override
	public int count() {
		return count(true);
	}
	
	@Sessional
	@Override
	public Role find(String name) {
		EntityCriteria<Role> criteria = newCriteria();
		criteria.add(Restrictions.eq("name", name));
		criteria.setCacheable(true);
		return dao.find(criteria);
	}
	
}
