package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultGroupManager extends BaseEntityManager<Group> implements GroupManager {

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
			
			settingManager.onRenameGroup(oldName, group.getName());
			issueFieldManager.onRenameGroup(oldName, group.getName());
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
			usedInProject.prefix("project '" + project.getPath() + "': setting");
			usage.add(usedInProject);
		}

		usage.add(settingManager.onDeleteGroup(group.getName()));
		
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

	private EntityCriteria<Group> getCriteria(@Nullable String term) {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (term != null) 
			criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}
	
	@Sessional
	@Override
	public List<Group> query(String term, int firstResult, int maxResults) {
		EntityCriteria<Group> criteria = getCriteria(term);
		criteria.addOrder(Order.asc("name"));
		return query(criteria, firstResult, maxResults);
	}

	@Sessional
	@Override
	public int count(String term) {
		return count(getCriteria(term));
	}

	@Sessional
	@Override
	public List<Group> queryAdminstrator() {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		criteria.add(Restrictions.eq(Group.PROP_ADMINISTRATOR, true));
		criteria.setCacheable(true);
		return query(criteria);
	}

}
