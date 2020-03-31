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
import io.onedev.server.model.support.role.AllIssueFields;
import io.onedev.server.model.support.role.ExcludeIssueFields;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.IssueFieldSet;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.model.support.role.NoneIssueFields;
import io.onedev.server.model.support.role.IncludeIssueFields;
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
	
	@Transactional
	public void setupDefaults() {
		Role manager = new Role();
		manager.setName("Manager");
		manager.setManageProject(true);
		save(manager, null);
		
		Role developer = new Role();
		developer.setName("Developer");
		developer.setCodePrivilege(CodePrivilege.WRITE);
		developer.setScheduleIssues(true);
		developer.setEditableIssueFields(new AllIssueFields());
		
		JobPrivilege jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setRunJob(true);
		developer.getJobPrivileges().add(jobPrivilege);
		
		save(developer, null);

		Role tester = new Role();
		tester.setName("Tester");
		tester.setCodePrivilege(CodePrivilege.READ);
		tester.setScheduleIssues(true);
		tester.setEditableIssueFields(new AllIssueFields());
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setAccessLog(true);
		tester.getJobPrivileges().add(jobPrivilege);
		
		save(tester, null);
		
		Role reporter = new Role();
		reporter.setName("Reporter");
		reporter.setCodePrivilege(CodePrivilege.NONE);
		ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
		allfieldsExcept.getExcludeFields().add("Assignees");
		reporter.setEditableIssueFields(allfieldsExcept);
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		reporter.getJobPrivileges().add(jobPrivilege);

		save(reporter, null);					
	}

	@Override
	public void onRenameIssueField(String oldName, String newName) {
		for (Role role: query()) {
			IssueFieldSet fieldSet = role.getEditableIssueFields();
			if (fieldSet instanceof ExcludeIssueFields) {
				ExcludeIssueFields allIssueFieldsExcept = (ExcludeIssueFields) fieldSet;
				if (allIssueFieldsExcept.getExcludeFields().remove(oldName))
					allIssueFieldsExcept.getExcludeFields().add(newName);
				if (allIssueFieldsExcept.getExcludeFields().isEmpty())
					role.setEditableIssueFields(new AllIssueFields());
			} else if (fieldSet instanceof IncludeIssueFields) {
				IncludeIssueFields specifiedIssueFields = (IncludeIssueFields) fieldSet;
				if (specifiedIssueFields.getIncludeFields().remove(oldName))
					specifiedIssueFields.getIncludeFields().add(newName);
				if (specifiedIssueFields.getIncludeFields().isEmpty())
					role.setEditableIssueFields(new NoneIssueFields());
			}
		}
	}

	@Override
	public void onDeleteIssueField(String fieldName) {
		for (Role role: query()) {
			IssueFieldSet fieldSet = role.getEditableIssueFields();
			if (fieldSet instanceof ExcludeIssueFields) {
				ExcludeIssueFields exludeIssueFields = (ExcludeIssueFields) fieldSet;
				exludeIssueFields.getExcludeFields().remove(fieldName);
				if (exludeIssueFields.getExcludeFields().isEmpty())
					role.setEditableIssueFields(new AllIssueFields());
			} else if (fieldSet instanceof IncludeIssueFields) {
				IncludeIssueFields includeIssueFields = (IncludeIssueFields) fieldSet;
				includeIssueFields.getIncludeFields().remove(fieldName);
				if (includeIssueFields.getIncludeFields().isEmpty())
					role.setEditableIssueFields(new NoneIssueFields());
			}
		}
	}
	
}
