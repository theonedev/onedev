package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.ReplicationMode;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Role;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.role.AllIssueFields;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.ExcludeIssueFields;
import io.onedev.server.model.support.role.IncludeIssueFields;
import io.onedev.server.model.support.role.IssueFieldSet;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.model.support.role.NoneIssueFields;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;

@Singleton
public class DefaultRoleManager extends BaseEntityManager<Role> implements RoleManager {

	private final SettingManager settingManager;
	
	private final IdManager idManager;
	
	@Inject
	public DefaultRoleManager(Dao dao, SettingManager settingManager, IdManager idManager) {
		super(dao);
		this.settingManager = settingManager;
		this.idManager = idManager;
	}

	@Transactional
	@Override
	public void replicate(Role role) {
		getSession().replicate(role, ReplicationMode.OVERWRITE);
		idManager.useId(Role.class, role.getId());
	}
	
	@Transactional
	@Override
	public void save(Role role, String oldName) {
		if (oldName != null && !oldName.equals(role.getName())) 
			settingManager.getIssueSetting().onRenameRole(oldName, role.getName());
		dao.persist(role);
	}

	@Transactional
	@Override
	public void delete(Role role) {
    	Usage usage = new Usage();

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
		criteria.add(Restrictions.ilike("name", name));
		criteria.setCacheable(true);
		return dao.find(criteria);
	}
	
	@Transactional
	public void setupDefaults() {
		Role codeWriter = new Role();
		codeWriter.setName("Code Writer");
		codeWriter.setCodePrivilege(CodePrivilege.WRITE);
		codeWriter.setScheduleIssues(true);
		codeWriter.setEditableIssueFields(new AllIssueFields());
		
		JobPrivilege jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setRunJob(true);
		codeWriter.getJobPrivileges().add(jobPrivilege);
		
		save(codeWriter, null);

		Role codeReader = new Role();
		codeReader.setName("Code Reader");
		codeReader.setCodePrivilege(CodePrivilege.READ);
		codeReader.setScheduleIssues(true);
		codeReader.setEditableIssueFields(new AllIssueFields());
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setAccessLog(true);
		codeReader.getJobPrivileges().add(jobPrivilege);
		
		save(codeReader, null);
		
		Role issueReporter = new Role();
		issueReporter.setName("Issue Reporter");
		issueReporter.setCodePrivilege(CodePrivilege.NONE);
		ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
		allfieldsExcept.getExcludeFields().add("Assignees");
		issueReporter.setEditableIssueFields(allfieldsExcept);
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		issueReporter.getJobPrivileges().add(jobPrivilege);

		save(issueReporter, null);					
	}

    @Sessional
    @Override
    public Role getOwner() {
    	return load(Role.OWNER_ID);
    }

	@Override
	public Collection<String> getUndefinedIssueFields() {
		Collection<String> undefinedFields = new HashSet<>();
		GlobalIssueSetting issueSetting = settingManager.getIssueSetting();
		for (Role role: query()) {
			IssueFieldSet fieldSet = role.getEditableIssueFields();
			if (fieldSet instanceof ExcludeIssueFields) {
				ExcludeIssueFields excludeIssueFields = (ExcludeIssueFields) fieldSet;
				for (String fieldName: excludeIssueFields.getExcludeFields()) {
					if (issueSetting.getFieldSpec(fieldName) == null)
						undefinedFields.add(fieldName);
				}
			} else if (fieldSet instanceof IncludeIssueFields) {
				IncludeIssueFields includeIssueFields = (IncludeIssueFields) fieldSet;
				for (String fieldName: includeIssueFields.getIncludeFields()) {
					if (issueSetting.getFieldSpec(fieldName) == null)
						undefinedFields.add(fieldName);
				}
			}
		}
		
		return undefinedFields;
	}

	@Override
	public void fixUndefinedIssueFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Role role: query()) {
			for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
				IssueFieldSet fieldSet = role.getEditableIssueFields();
				if (fieldSet instanceof ExcludeIssueFields) {
					ExcludeIssueFields excludeIssueFields = (ExcludeIssueFields) fieldSet;
					if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
						ReconcileUtils.renameItem(excludeIssueFields.getExcludeFields(), 
								entry.getKey(), entry.getValue().getNewField());
					} else {
						excludeIssueFields.getExcludeFields().remove(entry.getKey());
					}
					if (excludeIssueFields.getExcludeFields().isEmpty())
						role.setEditableIssueFields(new AllIssueFields());
				} else if (fieldSet instanceof IncludeIssueFields) {
					IncludeIssueFields includeIssueFields = (IncludeIssueFields) fieldSet;
					if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
						ReconcileUtils.renameItem(includeIssueFields.getIncludeFields(), 
								entry.getKey(), entry.getValue().getNewField());
					} else {
						includeIssueFields.getIncludeFields().remove(entry.getKey());
					}
					if (includeIssueFields.getIncludeFields().isEmpty())
						role.setEditableIssueFields(new NoneIssueFields());
				}
			}
		}
	}
	
}
