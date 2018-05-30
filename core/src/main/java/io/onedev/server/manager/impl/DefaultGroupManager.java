package io.onedev.server.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.authenticator.Authenticator;

@Singleton
public class DefaultGroupManager extends AbstractEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	private final ConfigManager configManager;
	
	private final CacheManager cacheManager;
	
	private final IssueFieldUnaryManager issueFieldManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, ConfigManager configManager, 
			CacheManager cacheManager, IssueFieldUnaryManager issueFieldManager) {
		super(dao);
		this.projectManager = projectManager;
		this.configManager = configManager;
		this.cacheManager = cacheManager;
		this.issueFieldManager = issueFieldManager;
	}

	@Transactional
	@Override
	public void save(Group group, String oldName) {
		if (oldName != null && !oldName.equals(group.getName())) {
			for (Project project: projectManager.findAll()) {
				for (BranchProtection protection: project.getBranchProtections()) 
					protection.onRenameGroup(oldName, group.getName());
				for (TagProtection protection: project.getTagProtections())
					protection.onRenameGroup(oldName, group.getName());
				project.getIssueWorkflow().onRenameGroup(oldName, group.getName());
			}
			
			Authenticator authenticator = configManager.getAuthenticator();
			if (authenticator != null) {
				authenticator.onRenameGroup(oldName, group.getName());
				configManager.saveAuthenticator(authenticator);
			}
			
			issueFieldManager.onRenameGroup(oldName, group.getName());
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
		for (Project project: projectManager.findAll()) {
			for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
				if (it.next().onDeleteGroup(group.getName()))
					it.remove();
			}
			for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) {
				if (it.next().onDeleteGroup(group.getName()))
					it.remove();
			}
			project.getIssueWorkflow().onDeleteGroup(group.getName());
		}
		Authenticator authenticator = configManager.getAuthenticator();
		if (authenticator != null) { 
			authenticator.onDeleteGroup(group.getName());
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
