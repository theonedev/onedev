package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.cache.CacheManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.Usage;

@Singleton
public class DefaultGroupManager extends AbstractEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	private final IssueFieldManager issueFieldManager;
	
	private final CacheManager cacheManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
			IssueFieldManager issueFieldManager, CacheManager cacheManager) {
		super(dao);
		this.projectManager = projectManager;
		this.settingManager = settingManager;
		this.issueFieldManager = issueFieldManager;
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
			
			issueFieldManager.onRenameGroup(oldName, group.getName());
			settingManager.getIssueSetting().onRenameGroup(oldName, group.getName());
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
    	Usage usage = new Usage();
		for (Project project: projectManager.query()) {
			for (BranchProtection protection: project.getBranchProtections()) 
				usage.add(protection.onDeleteGroup(group.getName()));
			for (TagProtection protection: project.getTagProtections()) 
				usage.add(protection.onDeleteGroup(group.getName()));
			usage.add(project.getIssueSetting().onDeleteGroup(group.getName()));
			usage.prefix("project '" + project.getName() + "': setting");
		}

		usage.add(settingManager.getIssueSetting().onDeleteGroup(group.getName()).prefix("administration"));

		Authenticator authenticator = settingManager.getAuthenticator();
		if (authenticator != null && authenticator.getDefaultGroupNames().contains(group.getName())) 
			usage.add("administration: authenticator");
		
		usage.checkInUse("Group '" + group.getName() + "'");
		
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
