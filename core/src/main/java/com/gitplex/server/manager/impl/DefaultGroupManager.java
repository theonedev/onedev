package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.authenticator.Authenticator;

@Singleton
public class DefaultGroupManager extends AbstractEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	private final ConfigManager configManager;
	
	private final CacheManager cacheManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, ConfigManager configManager, 
			CacheManager cacheManager) {
		super(dao);
		this.projectManager = projectManager;
		this.configManager = configManager;
		this.cacheManager = cacheManager;
	}

	@Transactional
	@Override
	public void save(Group group, String oldName) {
		if (oldName != null && !oldName.equals(group.getName())) {
			for (Project project: projectManager.findAll()) {
				for (BranchProtection protection: project.getBranchProtections()) 
					protection.onGroupRename(oldName, group.getName());
				for (TagProtection protection: project.getTagProtections())
					protection.onGroupRename(oldName, group.getName());
			}
			
			Authenticator authenticator = configManager.getAuthenticator();
			if (authenticator != null) {
				if (authenticator.onGroupRename(oldName, group.getName()))
					configManager.saveAuthenticator(authenticator);
			}
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
		for (Project project: projectManager.findAll()) {
			for (BranchProtection protection: project.getBranchProtections()) 
				protection.onGroupDelete(group.getName());
			for (TagProtection protection: project.getTagProtections())
				protection.onGroupDelete(group.getName());
		}
		Authenticator authenticator = configManager.getAuthenticator();
		if (authenticator != null) {
			if (authenticator.onGroupDelete(group.getName()))
				configManager.saveAuthenticator(authenticator);
		}
		
		dao.remove(group);
	}

	@Sessional
	@Override
	public Group find(String name) {
		Long id = cacheManager.getGroupIdByName(name);
		if (id != null) 
			return load(id);
		else
			return null;
	}
	
}
