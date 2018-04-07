package io.onedev.server.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.exception.InUseException;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.IssueFieldManager;
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
import io.onedev.server.util.UsageUtils;

@Singleton
public class DefaultGroupManager extends AbstractEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	private final ConfigManager configManager;
	
	private final CacheManager cacheManager;
	
	private final IssueFieldManager issueFieldManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, ConfigManager configManager, 
			CacheManager cacheManager, IssueFieldManager issueFieldManager) {
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
		List<String> usages = new ArrayList<>();
		for (Project project: projectManager.findAll()) {
			for (BranchProtection protection: project.getBranchProtections()) 
				usages.addAll(UsageUtils.prependCategory("Project '" + project.getName() + "' / Setting / Branch Protection", protection.onDeleteGroup(group.getName())));
			for (TagProtection protection: project.getTagProtections())   
				usages.addAll(UsageUtils.prependCategory("Project '" + project.getName() + "' / Setting / Tag Protection", protection.onDeleteGroup(group.getName())));
			usages.addAll(UsageUtils.prependCategory("Project '" + project.getName() + "' / Setting", project.getIssueWorkflow().onDeleteGroup(group.getName())));
		}
		Authenticator authenticator = configManager.getAuthenticator();
		if (authenticator != null) { 
			usages.addAll(UsageUtils.prependCategory("Administration / External Authentication", authenticator.onDeleteGroup(group.getName())));
			configManager.saveAuthenticator(authenticator);
		}
		
		dao.remove(group);
		
		if (!usages.isEmpty())
			throw new InUseException("Group '" + group.getName() + "'", usages);
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
