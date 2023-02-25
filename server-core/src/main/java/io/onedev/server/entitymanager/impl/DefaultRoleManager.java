package io.onedev.server.entitymanager.impl;

import com.hazelcast.core.HazelcastInstance;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.LinkAuthorizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.role.*;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.facade.RoleCache;
import io.onedev.server.util.facade.RoleFacade;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class DefaultRoleManager extends BaseEntityManager<Role> implements RoleManager {

	private final SettingManager settingManager;
	
	private final IdManager idManager;
	
	private final LinkAuthorizationManager linkAuthorizationManager;
	
	private final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;
	
	private final TransactionManager transactionManager;

	private volatile RoleCache cache;
	
	@Inject
	public DefaultRoleManager(Dao dao, SettingManager settingManager, IdManager idManager, 
							  LinkAuthorizationManager linkAuthorizationManager, 
							  ProjectManager projectManager, ClusterManager clusterManager, 
							  TransactionManager transactionManager) {
		super(dao);
		this.settingManager = settingManager;
		this.idManager = idManager;
		this.linkAuthorizationManager = linkAuthorizationManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
		this.transactionManager = transactionManager;
	}

	@Transactional
	@Override
	public void replicate(Role role) {
		getSession().replicate(role, ReplicationMode.OVERWRITE);
		idManager.useId(Role.class, role.getId());
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		cache = new RoleCache(hazelcastInstance.getReplicatedMap("roleCache"));

		for (var role: query())
			cache.put(role.getId(), role.getFacade());
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Role) {
			Long id = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					cache.remove(id);
				}

			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Role) {
			RoleFacade facade = ((Role)event.getEntity()).getFacade();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					if (cache != null)
						cache.put(facade.getId(), facade);
				}

			});
		}
	}

	@Transactional
	@Override
	public void save(Role role, Collection<LinkSpec> authorizedLinks, String oldName) {
		if (oldName != null && !oldName.equals(role.getName())) 
			settingManager.onRenameRole(oldName, role.getName());
		dao.persist(role);
		
		linkAuthorizationManager.syncAuthorizations(role, authorizedLinks);
	}

	@Transactional
	@Override
	public void delete(Role role) {
    	Usage usage = new Usage();

    	usage.add(settingManager.onDeleteRole(role.getName()));
		
		usage.checkInUse("Role '" + role.getName() + "'");
		
		for (Project project: role.getDefaultProjects()) {
			project.setDefaultRole(null);
			projectManager.save(project);
		}
    	
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
		RoleFacade facade = cache.find(name);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
	}
	
	@Transactional
	public void setupDefaults() {
		boolean hasAssigneesField = settingManager.getIssueSetting().getFieldSpec("Assignees") != null;
		
		Role codeWriter = new Role();
		codeWriter.setName("Code Writer");
		codeWriter.setCodePrivilege(CodePrivilege.WRITE);
		codeWriter.setScheduleIssues(true);
		codeWriter.setAccessConfidentialIssues(true);
		codeWriter.setEditableIssueFields(new AllIssueFields());
		
		JobPrivilege jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setRunJob(true);
		codeWriter.getJobPrivileges().add(jobPrivilege);
		
		save(codeWriter, new ArrayList<>(), null);

		Role codeReader = new Role();
		codeReader.setName("Code Reader");
		codeReader.setCodePrivilege(CodePrivilege.READ);
		
		if (hasAssigneesField) {
			ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
			allfieldsExcept.getExcludeFields().add("Assignees");
			codeReader.setEditableIssueFields(allfieldsExcept);
		}
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		codeReader.getJobPrivileges().add(jobPrivilege);
		
		save(codeReader, new ArrayList<>(), null);
		
		Role issueReporter = new Role();
		issueReporter.setName("Issue Reporter");
		issueReporter.setCodePrivilege(CodePrivilege.NONE);
		
		if (hasAssigneesField) {
			ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
			allfieldsExcept.getExcludeFields().add("Assignees");
			issueReporter.setEditableIssueFields(allfieldsExcept);
		}
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		issueReporter.getJobPrivileges().add(jobPrivilege);

		save(issueReporter, new ArrayList<>(), null);					
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
	
	private EntityCriteria<Role> getCriteria(@Nullable String term) {
		EntityCriteria<Role> criteria = EntityCriteria.of(Role.class);
		if (term != null) 
			criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}

	@Sessional
	@Override
	public List<Role> query(@Nullable String term, int firstResult, int maxResult) {
		EntityCriteria<Role> criteria = getCriteria(term);
		criteria.addOrder(Order.asc("name"));
		return query(criteria, firstResult, maxResult);
	}
	
	@Sessional
	@Override
	public int count(@Nullable String term) {
		return count(getCriteria(term));
	}
	
}
