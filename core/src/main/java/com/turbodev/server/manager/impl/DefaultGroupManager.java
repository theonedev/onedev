package com.turbodev.server.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.GroupManager;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.model.Group;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.support.BranchProtection;
import com.turbodev.server.model.support.TagProtection;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.security.authenticator.Authenticator;

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
			for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
				if (it.next().onGroupDelete(group.getName()))
					it.remove();
			}
			for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) { 
				if (it.next().onGroupDelete(group.getName()))
					it.remove();
			}
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
