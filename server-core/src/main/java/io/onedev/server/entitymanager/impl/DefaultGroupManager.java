package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultGroupManager extends BaseEntityManager<Group> implements GroupManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultGroupManager.class);
	
	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	private final IssueFieldManager issueFieldManager;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
			IssueFieldManager issueFieldManager) {
		super(dao);
		this.projectManager = projectManager;
		this.settingManager = settingManager;
		this.issueFieldManager = issueFieldManager;
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
			}
			
			Authenticator authenticator = settingManager.getAuthenticator();
			if (authenticator != null) {
				authenticator.onRenameGroup(oldName, group.getName());
				settingManager.saveAuthenticator(authenticator);
			}
			
			issueFieldManager.onRenameGroup(oldName, group.getName());
			settingManager.getIssueSetting().onRenameGroup(oldName, group.getName());
			settingManager.getSecuritySetting().onRenameGroup(oldName, group.getName());
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
    	Usage usage = new Usage();
		for (Project project: projectManager.query()) {
			Usage usedInProject = new Usage();
			for (BranchProtection protection: project.getBranchProtections()) 
				usedInProject.add(protection.onDeleteGroup(group.getName()));
			for (TagProtection protection: project.getTagProtections()) 
				usedInProject.add(protection.onDeleteGroup(group.getName()));
			usedInProject.prefix("project '" + project.getName() + "': setting");
			usage.add(usedInProject);
		}

		usage.add(settingManager.getIssueSetting().onDeleteGroup(group.getName()).prefix("administration"));

		Authenticator authenticator = settingManager.getAuthenticator();
		if (authenticator != null)
			usage.add(authenticator.onDeleteGroup(group.getName()).prefix("administration"));
		
		usage.add(settingManager.getSecuritySetting().onDeleteGroup(group.getName()).prefix("administration"));
		
		usage.checkInUse("Group '" + group.getName() + "'");
		
		dao.remove(group);
	}

	@Sessional
	@Override
	public Group find(String name) {
		EntityCriteria<Group> criteria = newCriteria();
		criteria.add(Restrictions.ilike("name", name));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Override
	public List<Group> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}

	@Override
	public Group findAnonymous() {
		SecuritySetting securitySetting = OneDev.getInstance(SettingManager.class).getSecuritySetting();
		if (securitySetting.isEnableAnonymousAccess()) {
			Group group = find(securitySetting.getAnonymousGroup());
			if (group != null) 
				return group;
			else
				logger.error("Undefined anonymous group: " + securitySetting.getAnonymousGroup());
		}
		return null;
	}
	
}
