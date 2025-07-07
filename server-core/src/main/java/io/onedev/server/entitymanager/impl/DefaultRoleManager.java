package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.ReplicationMode;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.LinkAuthorizationManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.role.AllIssueFields;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.ExcludeIssueFields;
import io.onedev.server.model.support.role.IncludeIssueFields;
import io.onedev.server.model.support.role.IssueFieldSet;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.model.support.role.NoneIssueFields;
import io.onedev.server.model.support.role.PackPrivilege;
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

@Singleton
public class DefaultRoleManager extends BaseEntityManager<Role> implements RoleManager {

	private final SettingManager settingManager;
	
	private final IdManager idManager;

	private final LinkSpecManager linkSpecManager;
	
	private final LinkAuthorizationManager linkAuthorizationManager;
		
	private final ClusterManager clusterManager;
	
	private final TransactionManager transactionManager;

	private volatile RoleCache cache;
	
	@Inject
	public DefaultRoleManager(Dao dao, SettingManager settingManager, IdManager idManager, 
							  LinkAuthorizationManager linkAuthorizationManager, 
							  ClusterManager clusterManager, TransactionManager transactionManager, 
							  LinkSpecManager linkSpecManager) {
		super(dao);
		this.settingManager = settingManager;
		this.idManager = idManager;
		this.linkAuthorizationManager = linkAuthorizationManager;
		this.clusterManager = clusterManager;
		this.transactionManager = transactionManager;
		this.linkSpecManager = linkSpecManager;
	}

	@Transactional
	@Override
	public void replicate(Role role) {
		getSession().replicate(role, ReplicationMode.OVERWRITE);
		idManager.useId(Role.class, role.getId());

		var facade = role.getFacade();
		transactionManager.runAfterCommit(() -> cache.put(facade.getId(), facade));
	}

	@Sessional
	@Listen
	public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		cache = new RoleCache(hazelcastInstance.getMap("roleCache"));

		IAtomicLong cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("roleCacheInited"); 
		clusterManager.initWithLead(cacheInited, () -> {
			for (var role: query())
				cache.put(role.getId(), role.getFacade());
			return 1L;			
		});
	}

	@Transactional
	@Override
	public void create(Role role, Collection<LinkSpec> authorizedLinks) {
		Preconditions.checkState(role.isNew());
		dao.persist(role);

		if (authorizedLinks != null)
			linkAuthorizationManager.syncAuthorizations(role, authorizedLinks);
	}

	@Transactional
	@Override
	public void update(Role role, Collection<LinkSpec> authorizedLinks, String oldName) {
		Preconditions.checkState(!role.isNew());
		
		if (oldName != null && !oldName.equals(role.getName())) 
			settingManager.onRenameRole(oldName, role.getName());
		dao.persist(role);		

		if (authorizedLinks != null)
			linkAuthorizationManager.syncAuthorizations(role, authorizedLinks);
	}

	@Transactional
	@Override
	public void delete(Role role) {
    	Usage usage = new Usage();

    	usage.add(settingManager.onDeleteRole(role.getName()));		
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
		codeWriter.setPackPrivilege(PackPrivilege.WRITE);
		codeWriter.setScheduleIssues(true);
		codeWriter.setAccessConfidentialIssues(true);
		codeWriter.setEditableIssueFields(new AllIssueFields());
		
		JobPrivilege jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setRunJob(true);
		codeWriter.getJobPrivileges().add(jobPrivilege);
		
		create(codeWriter, linkSpecManager.query());

		Role codeReader = new Role();
		codeReader.setName("Code Reader");
		codeReader.setCodePrivilege(CodePrivilege.READ);
		codeReader.setPackPrivilege(PackPrivilege.READ);
		codeReader.setAccessTimeTracking(false);
		
		if (hasAssigneesField) {
			ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
			allfieldsExcept.getExcludeFields().add("Assignees");
			codeReader.setEditableIssueFields(allfieldsExcept);
		}

		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		codeReader.getJobPrivileges().add(jobPrivilege);

		create(codeReader, new ArrayList<>());
		
		Role packWriter = new Role();
		packWriter.setName("Package Writer");
		packWriter.setCodePrivilege(CodePrivilege.READ);
		packWriter.setPackPrivilege(PackPrivilege.WRITE);
		packWriter.setScheduleIssues(true);
		packWriter.setAccessConfidentialIssues(true);
		packWriter.setEditableIssueFields(new AllIssueFields());

		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		jobPrivilege.setRunJob(true);
		packWriter.getJobPrivileges().add(jobPrivilege);

		create(packWriter, new ArrayList<>());

		Role packReader = new Role();
		packReader.setName("Package Reader");
		packReader.setPackPrivilege(PackPrivilege.READ);
		packReader.setAccessTimeTracking(false);
		
		if (hasAssigneesField) {
			ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
			allfieldsExcept.getExcludeFields().add("Assignees");
			packReader.setEditableIssueFields(allfieldsExcept);
		}
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		packReader.getJobPrivileges().add(jobPrivilege);
		
		create(packReader, new ArrayList<>());

		Role issueManager = new Role();
		issueManager.setName("Issue Manager");
		issueManager.setManageIssues(true);
		issueManager.setCodePrivilege(CodePrivilege.READ);
		issueManager.setPackPrivilege(PackPrivilege.READ);

		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		issueManager.getJobPrivileges().add(jobPrivilege);

		create(issueManager, new ArrayList<>());

		Role issueReporter = new Role();
		issueReporter.setName("Issue Reporter");
		issueReporter.setCodePrivilege(CodePrivilege.NONE);
		issueReporter.setAccessTimeTracking(false);
		
		if (hasAssigneesField) {
			ExcludeIssueFields allfieldsExcept = new ExcludeIssueFields();
			allfieldsExcept.getExcludeFields().add("Assignees");
			issueReporter.setEditableIssueFields(allfieldsExcept);
		}
		
		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		issueReporter.getJobPrivileges().add(jobPrivilege);

		create(issueReporter, new ArrayList<>());					
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

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Role) {
			var facade = ((Role) event.getEntity()).getFacade();
			transactionManager.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Role) {
			var id = event.getEntity().getId();
			transactionManager.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}
