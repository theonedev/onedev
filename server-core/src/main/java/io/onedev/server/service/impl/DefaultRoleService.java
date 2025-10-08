package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.ReplicationMode;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;

import io.onedev.server.cluster.ClusterService;
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
import io.onedev.server.persistence.IdService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.LinkAuthorizationService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.facade.RoleCache;
import io.onedev.server.util.facade.RoleFacade;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;

@Singleton
public class DefaultRoleService extends BaseEntityService<Role> implements RoleService {

	@Inject
	private SettingService settingService;

	@Inject
	private IdService idService;

	@Inject
	private LinkSpecService linkSpecService;

	@Inject
	private LinkAuthorizationService linkAuthorizationService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private TransactionService transactionService;

	private volatile RoleCache cache;

	@Transactional
	@Override
	public void replicate(Role role) {
		getSession().replicate(role, ReplicationMode.OVERWRITE);
		idService.useId(Role.class, role.getId());

		var facade = role.getFacade();
		transactionService.runAfterCommit(() -> cache.put(facade.getId(), facade));
	}

	@Sessional
	@Listen
	public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
		cache = new RoleCache(hazelcastInstance.getMap("roleCache"));

		IAtomicLong cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("roleCacheInited"); 
		clusterService.initWithLead(cacheInited, () -> {
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
			linkAuthorizationService.syncAuthorizations(role, authorizedLinks);
	}

	@Transactional
	@Override
	public void update(Role role, Collection<LinkSpec> authorizedLinks, String oldName) {
		Preconditions.checkState(!role.isNew());
		
		if (oldName != null && !oldName.equals(role.getName())) 
			settingService.onRenameRole(oldName, role.getName());
		dao.persist(role);		

		if (authorizedLinks != null)
			linkAuthorizationService.syncAuthorizations(role, authorizedLinks);
	}

	@Transactional
	@Override
	public void delete(Role role) {
    	Usage usage = new Usage();

    	usage.add(settingService.onDeleteRole(role.getName()));		
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
		boolean hasAssigneesField = settingService.getIssueSetting().getFieldSpec("Assignees") != null;
		
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
		
		create(codeWriter, linkSpecService.query());

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

		Role issueService = new Role();
		issueService.setName("Issue Manager");
		issueService.setManageIssues(true);
		issueService.setCodePrivilege(CodePrivilege.READ);
		issueService.setPackPrivilege(PackPrivilege.READ);

		jobPrivilege = new JobPrivilege();
		jobPrivilege.setJobNames("*");
		issueService.getJobPrivileges().add(jobPrivilege);

		create(issueService, new ArrayList<>());

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
		GlobalIssueSetting issueSetting = settingService.getIssueSetting();
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
			transactionService.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Role) {
			var id = event.getEntity().getId();
			transactionService.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}
