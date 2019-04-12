package io.onedev.server.entitymanager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.cache.CacheManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueFieldEntityManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.authenticator.Authenticator;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultGroupManager extends AbstractEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	private final IssueFieldEntityManager issueFieldEntityManager;
	
	private final CacheManager cacheManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
			IssueFieldEntityManager issueFieldEntityManager, CacheManager cacheManager) {
		super(dao);
		this.projectManager = projectManager;
		this.settingManager = settingManager;
		this.issueFieldEntityManager = issueFieldEntityManager;
		this.cacheManager = cacheManager;
	}

	@Transactional
	@Override
	public void save(Group group, String oldName) {
		if (oldName != null && !oldName.equals(group.getName())) {
			for (Project project: projectManager.query()) {
				for (BranchProtection protection: project.getBranchProtections()) 
					protection.onRenameGroup(oldName, group.getName());
				for (TagProtection protection: project.getTagProtections())
					protection.onRenameGroup(oldName, group.getName());
				project.getIssueSetting().onRenameGroup(oldName, group.getName());
			}
			
			Authenticator authenticator = settingManager.getAuthenticator();
			if (authenticator != null) {
				authenticator.onRenameGroup(oldName, group.getName());
				settingManager.saveAuthenticator(authenticator);
			}
			
			issueFieldEntityManager.onRenameGroup(oldName, group.getName());
			settingManager.getIssueSetting().onRenameGroup(oldName, group.getName());
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
		for (Project project: projectManager.query()) {
			for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
				if (it.next().onDeleteGroup(group.getName()))
					it.remove();
			}
			for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) {
				if (it.next().onDeleteGroup(group.getName()))
					it.remove();
			}
			project.getIssueSetting().onDeleteGroup(group.getName());
		}
		Authenticator authenticator = settingManager.getAuthenticator();
		if (authenticator != null) { 
			authenticator.onDeleteGroup(group.getName());
			settingManager.saveAuthenticator(authenticator);
		}
		settingManager.getIssueSetting().onDeleteGroup(group.getName());
		
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
