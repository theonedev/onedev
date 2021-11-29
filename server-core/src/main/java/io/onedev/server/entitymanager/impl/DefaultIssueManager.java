package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Objects;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.loader.Listen;
import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.IssueScheduleManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.EntityReferenceManager;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.changedata.IssueChangeData;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.storage.AttachmentStorageManager;
import io.onedev.server.util.MilestoneAndState;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.facade.IssueFacade;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends BaseEntityManager<Issue> implements IssueManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueManager.class);
	
	private final IssueFieldManager issueFieldManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueQueryPersonalizationManager issueQueryPersonalizationManager;
	
	private final AttachmentStorageManager attachmentStorageManager;
	
	private final IssueCommentManager issueCommentManager;
	
	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final TransactionManager transactionManager;
	
	private final EntityReferenceManager entityReferenceManager;
	
	private final RoleManager roleManager;
	
	private final Map<Long, IssueFacade> cache = new HashMap<>();
	
	private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager issueFieldManager, 
			TransactionManager transactionManager, IssueQueryPersonalizationManager issueQueryPersonalizationManager, 
			SettingManager settingManager, ListenerRegistry listenerRegistry, 
			ProjectManager projectManager, UserManager userManager, 
			RoleManager roleManager, AttachmentStorageManager attachmentStorageManager, 
			IssueCommentManager issueCommentManager, EntityReferenceManager entityReferenceManager, 
			IssueScheduleManager issueScheduleManager) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
		this.issueQueryPersonalizationManager = issueQueryPersonalizationManager;
		this.listenerRegistry = listenerRegistry;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
		this.userManager = userManager;
		this.roleManager = roleManager;
		this.attachmentStorageManager = attachmentStorageManager;
		this.issueCommentManager = issueCommentManager;
		this.entityReferenceManager = entityReferenceManager;
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching issue info...");
		
		Query<?> query = dao.getSession().createQuery("select id, project.id, number from Issue");
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long issueId = (Long) fields[0];
			cache.put(issueId, new IssueFacade(issueId, (Long)fields[1], (Long)fields[2]));
		}
	}
	
	@Sessional
	@Override
	public Issue find(Project project, long number) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.eq(Issue.PROP_NUMBER_SCOPE, project.getForkRoot()));
		criteria.add(Restrictions.eq(Issue.PROP_NUMBER, number));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Override
	public Issue findByUUID(String uuid) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.eq(Issue.PROP_UUID, uuid));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Issue findByFQN(String fqn) {
		return find(ProjectScopedNumber.from(fqn));
	}
	
	@Sessional
	@Override
	public Issue find(ProjectScopedNumber fqn) {
		return find(fqn.getProject(), fqn.getNumber());
	}
	
	@Transactional
	@Override
	public void open(Issue issue) {
		Preconditions.checkArgument(issue.isNew());
		issue.setNumberScope(issue.getProject().getForkRoot());
		issue.setNumber(getNextNumber(issue.getNumberScope()));
		
		IssueOpened event = new IssueOpened(issue);
		issue.setLastUpdate(event.getLastUpdate());
		
		save(issue);

		issueFieldManager.saveFields(issue);
		for (IssueSchedule schedule: issue.getSchedules())
			dao.persist(schedule);
		
		listenerRegistry.post(event);
	}

	@Sessional
	public Long getNextNumber(Project numberScope) {
		Query<?> query = getSession().createQuery(String.format("select max(%s) from Issue where %s=:numberScope", 
				Issue.PROP_NUMBER, Issue.PROP_NUMBER_SCOPE));
		query.setParameter(Issue.PROP_NUMBER_SCOPE, numberScope);
		return getNextNumber(numberScope, query);
	}
	
	@Transactional
	@Override
	public void save(Issue issue) {
		super.save(issue);
		
		IssueFacade facade = issue.getFacade();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				cacheLock.writeLock().lock();
				try {
					cache.put(facade.getId(), facade);
				} finally {
					cacheLock.writeLock().unlock();
				}
			}
			
		});
	}

	private List<javax.persistence.criteria.Order> getOrders(List<EntitySort> sorts, CriteriaBuilder builder, Root<Issue> root) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: sorts) {
			if (Issue.ORDER_FIELDS.containsKey(sort.getField())) {
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(IssueQuery.getPath(root, Issue.ORDER_FIELDS.get(sort.getField()))));
				else
					orders.add(builder.desc(IssueQuery.getPath(root, Issue.ORDER_FIELDS.get(sort.getField()))));
			} else {
				Join<Issue, IssueField> join = root.join(Issue.PROP_FIELDS, JoinType.LEFT);
				join.on(builder.equal(join.get(IssueField.PROP_NAME), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get(IssueField.PROP_ORDINAL)));
				else
					orders.add(builder.desc(join.get(IssueField.PROP_ORDINAL)));
			}
		}

		if (orders.isEmpty()) {
			orders.add(builder.desc(IssueQuery.getPath(root, Issue.PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE)));
		}
		return orders;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return settingManager.getIssueSetting();
	}

	@Sessional
	@Override
	public List<Issue> query(EntityQuery<Issue> issueQuery, int firstResult, int maxResults, 
			boolean loadFields) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> criteriaQuery = builder.createQuery(Issue.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		criteriaQuery.where(getPredicates(issueQuery.getCriteria(), criteriaQuery, builder, root));
		criteriaQuery.orderBy(getOrders(issueQuery.getSorts(), builder, root));
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (loadFields && !issues.isEmpty())
			issueFieldManager.populateFields(issues);
		
		return issues;
	}
	
	@Sessional
	@Override
	public List<Issue> query(Project project, boolean inTree, EntityQuery<Issue> issueQuery, 
			int firstResult, int maxResults, boolean loadFields) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> criteriaQuery = builder.createQuery(Issue.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		criteriaQuery.where(getPredicates(project, inTree, issueQuery.getCriteria(), criteriaQuery, builder, root));
		criteriaQuery.orderBy(getOrders(issueQuery.getSorts(), builder, root));
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (loadFields && !issues.isEmpty())
			issueFieldManager.populateFields(issues);
		
		return issues;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		boolean minorChange = false;
		if (event instanceof IssueChangeEvent) {
			IssueChangeData changeData = ((IssueChangeEvent)event).getChange().getData();
			if (changeData instanceof ReferencedFromAware) 
				minorChange = true;
		}

		if (!(event instanceof IssueOpened || minorChange))
			event.getIssue().setLastUpdate(event.getLastUpdate());
	}
	
	@Sessional
	@Override
	public int count(IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		criteriaQuery.where(getPredicates(issueCriteria, criteriaQuery, builder, root));
		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public int count(Project project, boolean inTree, IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		criteriaQuery.where(getPredicates(project, inTree, issueCriteria, criteriaQuery, builder, root));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}
	
	private Predicate[] getPredicates(Project project, boolean inTree, @Nullable io.onedev.server.search.entity.EntityCriteria<Issue> issueCriteria, 
			CriteriaQuery<?> query, CriteriaBuilder builder, Root<Issue> root) {
		List<Predicate> predicates = new ArrayList<>();
		Join<Project, Project> join = root.join(Issue.PROP_PROJECT, JoinType.INNER);		
		if (inTree)
			predicates.add(projectManager.getTreePredicate(builder, join, project));
		else
			predicates.add(builder.equal(root.get(Issue.PROP_PROJECT), project));
		if (issueCriteria != null)
			predicates.add(issueCriteria.getPredicate(query, root, builder));

		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private Predicate[] getPredicates(@Nullable io.onedev.server.search.entity.EntityCriteria<Issue> issueCriteria, 
			CriteriaQuery<?> query, CriteriaBuilder builder, Root<Issue> root) {
		List<Predicate> predicates = new ArrayList<>();
		if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new AccessProject()); 
			if (!projects.isEmpty())
				predicates.add(root.get(Issue.PROP_PROJECT).in(projects));
			else
				predicates.add(builder.disjunction());
		}
		if (issueCriteria != null)
			predicates.add(issueCriteria.getPredicate(query, root, builder));

		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Collection<String> getUndefinedStates() {
		Collection<String> undefinedStates = getIssueSetting().getUndefinedStates();
		
		Query<String> query = getSession().createQuery("select distinct state from Issue");
		
		for (String state: query.getResultList()) {
			if (getIssueSetting().getStateSpec(state) == null)
				undefinedStates.add(state);
		}

		for (Project project: projectManager.query()) {
			undefinedStates.addAll(project.getIssueSetting().getUndefinedStates(project));
			undefinedStates.addAll(project.getBuildSetting().getUndefinedStates(project));
		}
		
		for (IssueQueryPersonalization setting: issueQueryPersonalizationManager.query()) 
			populateUndefinedStates(undefinedStates, setting.getProject(), setting.getQueries());
		
		for (User user: userManager.query()) 
			populateUndefinedStates(undefinedStates, null, user.getIssueQueryPersonalization().getQueries());

		return undefinedStates;
	}
	
	private void populateUndefinedStates(Collection<String> undefinedStates, @Nullable Project project, 
			List<NamedIssueQuery> namedQueries) {
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				undefinedStates.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true).getUndefinedStates());
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(settingManager.getUndefinedIssueFields());
		undefinedFields.addAll(roleManager.getUndefinedIssueFields());
		
		Query<String> query = getSession().createQuery("select distinct name from IssueField");
		for (String fieldName: query.getResultList()) {
			FieldSpec field = getIssueSetting().getFieldSpec(fieldName);
			if (field == null)
				undefinedFields.add(fieldName);
		}

		for (Project project: projectManager.query()) { 
			undefinedFields.addAll(project.getIssueSetting().getUndefinedFields(project));
			undefinedFields.addAll(project.getBuildSetting().getUndefinedFields(project));
		}
		
		for (IssueQueryPersonalization setting: issueQueryPersonalizationManager.query()) 
			populateUndefinedFields(undefinedFields, setting.getProject(), setting.getQueries());
		
		for (User user: userManager.query()) 
			populateUndefinedFields(undefinedFields, null, user.getIssueQueryPersonalization().getQueries());
		
		return undefinedFields;
	}
	
	private void populateUndefinedFields(Collection<String> undefinedFields, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true).getUndefinedFields());
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		
		undefinedFieldValues.addAll(settingManager.getUndefinedIssueFieldValues());
		
		Query query = getSession().createQuery("select distinct name, value from IssueField where type=:choice");
		query.setParameter("choice", FieldSpec.ENUMERATION);
		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String fieldName = (String) row[0];
			String fieldValue = (String) row[1];
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(fieldName));
			if (specifiedChoices != null && fieldValue != null 
					&& !specifiedChoices.getChoiceValues().contains(fieldValue)) {
				undefinedFieldValues.add(new UndefinedFieldValue(fieldName, fieldValue));
			}
		}

		for (Project project: projectManager.query()) {
			undefinedFieldValues.addAll(project.getIssueSetting().getUndefinedFieldValues(project));
			undefinedFieldValues.addAll(project.getBuildSetting().getUndefinedFieldValues(project));
		}
		
		for (IssueQueryPersonalization setting: issueQueryPersonalizationManager.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, setting.getProject(), setting.getQueries());
		
		for (User user: userManager.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, null, user.getIssueQueryPersonalization().getQueries());
		
		return undefinedFieldValues;
	}
	
	private void populateUndefinedFieldValues(Collection<UndefinedFieldValue> undefinedFieldValues, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				undefinedFieldValues.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true).getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
	}

	@Transactional
	@Override
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		getIssueSetting().fixUndefinedStates(resolutions);
		
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedStateResolution.FixType.CHANGE_TO_ANOTHER_STATE) {
				Query<?> query = getSession().createQuery("update Issue set state=:newState where state=:oldState");
				query.setParameter("oldState", entry.getKey());
				query.setParameter("newState", entry.getValue().getNewState());
				query.executeUpdate();
			} else {
				Query<?> query = getSession().createQuery("delete from IssueField where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from IssueComment where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from IssueChange where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from IssueVote where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from IssueWatch where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from Issue where state=:state");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
			}
		}
		
		for (Project project: projectManager.query()) { 
			project.getIssueSetting().fixUndefinedStates(project, resolutions);
			project.getBuildSetting().fixUndefinedStates(project, resolutions);
		}
		
		for (IssueQueryPersonalization setting: issueQueryPersonalizationManager.query()) 
			fixUndefinedStates(setting.getProject(), resolutions, setting.getQueries());

		for (User user: userManager.query())
			fixUndefinedStates(null, resolutions, user.getIssueQueryPersonalization().getQueries());
	}
	
	private void fixUndefinedStates(@Nullable Project project, Map<String, UndefinedStateResolution> resolutions, 
			List<NamedIssueQuery> namedQueries) {
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true);
				if (parsedQuery.fixUndefinedStates(resolutions))
					namedQuery.setQuery(parsedQuery.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}

	@Transactional
	@Override
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		roleManager.fixUndefinedIssueFields(resolutions);
		
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			Query<?> query;
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				query = getSession().createQuery("update IssueField set name=:newName where name=:oldName");
				query.setParameter("oldName", entry.getKey());
				query.setParameter("newName", entry.getValue().getNewField());
			} else {
				query = getSession().createQuery("delete from IssueField where name=:fieldName");
				query.setParameter("fieldName", entry.getKey());
			}				
			query.executeUpdate();
		}
		
		for (Project project: projectManager.query()) { 
			project.getIssueSetting().fixUndefinedFields(project, resolutions);
			project.getBuildSetting().fixUndefinedFields(project, resolutions);
		}
		
		for (IssueQueryPersonalization setting: issueQueryPersonalizationManager.query())
			fixUndefinedFields(resolutions, setting.getProject(), setting.getQueries());
		
		for (User user: userManager.query()) 
			fixUndefinedFields(resolutions, null, user.getIssueQueryPersonalization().getQueries());
		
		Map<String, UndefinedFieldResolution> derivedDeletions = 
				getFieldResolutions(settingManager.fixUndefinedIssueFields(resolutions));
		if (!derivedDeletions.isEmpty())
			fixUndefinedFields(derivedDeletions);
	}
	
	private void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true);
				if (parsedQuery.fixUndefinedFields(resolutions))
					namedQuery.setQuery(parsedQuery.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@Override
	public void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldValuesResolution> resolutionEntry: resolutions.entrySet()) {
			for (String deletion: resolutionEntry.getValue().getDeletions()) {
				Query query = getSession().createQuery("delete from IssueField where name=:fieldName and value=:fieldValue");
				query.setParameter("fieldName", resolutionEntry.getKey());
				query.setParameter("fieldValue", deletion);
				query.executeUpdate();
			}
			for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) {
				Query query = getSession().createQuery("update IssueField set value=:newValue where name=:fieldName and value=:oldValue");
				query.setParameter("fieldName", resolutionEntry.getKey());
				query.setParameter("oldValue", renameEntry.getKey());
				query.setParameter("newValue", renameEntry.getValue());
				query.executeUpdate();
			}
		}
		
		for (Project project: projectManager.query()) {
			project.getIssueSetting().fixUndefinedFieldValues(project, resolutions);
			project.getBuildSetting().fixUndefinedFieldValues(project, resolutions);
		}
		
		for (IssueQueryPersonalization setting: issueQueryPersonalizationManager.query()) 
			fixUndefinedFieldValues(resolutions, setting.getProject(), setting.getQueries());
		
		for (User user: userManager.query())
			fixUndefinedFieldValues(resolutions, null, user.getIssueQueryPersonalization().getQueries());
		
		Map<String, UndefinedFieldResolution> derivedDeletions = 
				getFieldResolutions(settingManager.fixUndefinedIssueFieldValues(resolutions));
		if (!derivedDeletions.isEmpty())
			fixUndefinedFields(derivedDeletions);
	}
	
	private void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true);
				if (query.fixUndefinedFieldValues(resolutions))
					namedQuery.setQuery(query.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Transactional
	@Override
	public void fixFieldValueOrders() {
		Query query = getSession().createQuery("select distinct name, value, ordinal from IssueField where type=:choice");
		query.setParameter("choice", FieldSpec.ENUMERATION);

		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String name = (String) row[0];
			String value = (String) row[1];
			long ordinal = (long) row[2];
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(name));
			if (specifiedChoices != null) {
				long newOrdinal = specifiedChoices.getChoiceValues().indexOf(value);
				if (ordinal != newOrdinal) {
					query = getSession().createQuery("update IssueField set ordinal=:newOrdinal where name=:fieldName and value=:fieldValue");
					query.setParameter("fieldName", name);
					query.setParameter("fieldValue", value);
					query.setParameter("newOrdinal", newOrdinal);
					query.executeUpdate();
				}
			}
		}
	}
	
	private Map<String, UndefinedFieldResolution> getFieldResolutions(Collection<String> deletedFields) {
		Map<String, UndefinedFieldResolution> resolutions = new HashMap<>();
		for (String field: deletedFields) {
			UndefinedFieldResolution resolution = new UndefinedFieldResolution();
			resolution.setFixType(UndefinedFieldResolution.FixType.DELETE_THIS_FIELD);
			resolutions.put(field, resolution);
		}
		return resolutions;
	}

	@Sessional
	@Override
	public List<Issue> query(EntityQuery<Issue> scope, Project project, String term, int count) {
		if (term.contains("#")) {
			String projectPath = StringUtils.substringBefore(term, "#");
			Project specifiedProject = projectManager.find(projectPath);
			if (specifiedProject != null && SecurityUtils.canAccess(specifiedProject)) {
				project = specifiedProject;
				term = StringUtils.substringAfter(term, "#");
			}
		}
				
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> criteriaQuery = builder.createQuery(Issue.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (scope != null)
			predicates.addAll(Arrays.asList(getPredicates(scope.getCriteria(), criteriaQuery, builder, root)));		
		
		List<Predicate> projectPredicates = new ArrayList<>();
		projectPredicates.add(builder.equal(root.get(Issue.PROP_PROJECT), project));
		for (Project forkParent: project.getForkParents()) {
			if (SecurityUtils.canAccess(forkParent)) 
				projectPredicates.add(builder.equal(root.get(Issue.PROP_PROJECT), forkParent));
		}
		predicates.add(builder.or(projectPredicates.toArray(new Predicate[0])));
		
		if (term.startsWith("#"))
			term = term.substring(1);
		if (term.length() != 0) {
			try {
				long buildNumber = Long.parseLong(term);
				predicates.add(builder.equal(root.get(Issue.PROP_NUMBER), buildNumber));
			} catch (NumberFormatException e) {
				predicates.add(builder.or(
						builder.like(builder.lower(root.get(Issue.PROP_TITLE)), "%" + term.toLowerCase() + "%"),
						builder.like(builder.lower(root.get(Issue.PROP_NO_SPACE_TITLE)), "%" + term.toLowerCase() + "%")));
			}
		}

		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(
				builder.desc(root.get(Issue.PROP_PROJECT)), 
				builder.desc(root.get(Issue.PROP_NUMBER)));
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);
		return query.getResultList();
	}
	
	@Transactional
	@Override
	public void delete(Issue issue) {
		super.delete(issue);
		
		Long issueId = issue.getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				cacheLock.writeLock().lock();
				try {
					cache.remove(issueId);
				} finally {
					cacheLock.writeLock().unlock();
				}
			}
		});
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					cacheLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, IssueFacade>> it = cache.entrySet().iterator(); it.hasNext();) {
							IssueFacade issue = it.next().getValue();
							if (issue.getProjectId().equals(projectId))
								it.remove();
						}
					} finally {
						cacheLock.writeLock().unlock();
					}
				}
			});
		}
	}

	@Override
	public Collection<Long> getIssueNumbers(Long projectId) {
		cacheLock.readLock().lock();
		try {
			Collection<Long> issueNumbers = new HashSet<>();
			for (IssueFacade issue: cache.values()) {
				if (projectId.equals(issue.getProjectId()))
					issueNumbers.add(issue.getNumber());
			}
			return issueNumbers;
		} finally {
			cacheLock.readLock().unlock();
		}
	}

	@Sessional
	@Override
	public Collection<MilestoneAndState> queryMilestoneAndStates(Project project, Collection<Milestone> milestones) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<MilestoneAndState> criteriaQuery = builder.createQuery(MilestoneAndState.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);
		Join<Issue, Issue> issueJoin = root.join(IssueSchedule.PROP_ISSUE, JoinType.INNER);
		criteriaQuery.multiselect(
				root.get(IssueSchedule.PROP_MILESTONE).get(Milestone.PROP_ID), 
				issueJoin.get(Issue.PROP_STATE));
		
		List<Predicate> milestonePredicates = new ArrayList<>();
		for (Milestone milestone: milestones) 
			milestonePredicates.add(builder.equal(root.get(IssueSchedule.PROP_MILESTONE), milestone));
		
		Join<Project, Project> projectJoin = issueJoin.join(Issue.PROP_PROJECT, JoinType.INNER);
		criteriaQuery.where(builder.and(
				projectManager.getTreePredicate(builder, projectJoin, project),
				builder.or(milestonePredicates.toArray(new Predicate[0]))));
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}
	
	@Sessional
	@Override
	public Collection<Milestone> queryUsedMilestones(Project project) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Milestone> criteriaQuery = builder.createQuery(Milestone.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);
		criteriaQuery.select(root.get(IssueSchedule.PROP_MILESTONE));
		
		Join<Project, Project> join = root
				.join(IssueSchedule.PROP_ISSUE, JoinType.INNER)
				.join(Issue.PROP_PROJECT, JoinType.INNER);
		criteriaQuery.where(projectManager.getTreePredicate(builder, join, project));
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}
	
	@Transactional
	@Override
	public void move(Project targetProject, Collection<Issue> issues) {
		List<Pair<Project, String>> attachmentInfos = new ArrayList<>();
		Map<Long, Long> numberMapping = new HashMap<>();
		List<Issue> issueList = new ArrayList<>(issues);
		Collections.sort(issueList);
		for (Issue issue: issueList) {
			attachmentInfos.add(new Pair<>(issue.getAttachmentProject(), issue.getAttachmentGroup()));
			if (issue.getDescription() != null) {
				issue.setDescription(issue.getDescription().replace(
						issue.getAttachmentProject().getId() + "/attachment/" + issue.getAttachmentGroup(), 
						targetProject.getId() + "/attachment/" + issue.getAttachmentGroup()));
			}
			for (IssueComment comment: issue.getComments()) {
				comment.setContent(comment.getContent().replace(
						issue.getAttachmentProject().getId() + "/attachment/" + issue.getAttachmentGroup(), 
						targetProject.getId() + "/attachment/" + issue.getAttachmentGroup()));
			}

			Project numberScope = targetProject.getForkRoot();
			Long nextNumber = getNextNumber(numberScope);
			issue.setProject(targetProject);
			issue.setNumberScope(numberScope);
			Long oldNumber = issue.getNumber();
			issue.setNumber(nextNumber);
			numberMapping.put(oldNumber, nextNumber);
			
			for (IssueSchedule schedule: issue.getSchedules()) {
				if (schedule.getMilestone() != null 
						&& !schedule.getMilestone().getProject().isSelfOrAncestorOf(targetProject)) {
					dao.remove(schedule);
				}
			}
			
		}
		
		for (Issue issue: issueList) {
			if (issue.getDescription() != null) {
				issue.setDescription(new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(issue.getDescription(), "#"));
			}
			
			for (IssueComment comment: issue.getComments()) {
				comment.setContent(new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(comment.getContent(), "#"));
				issueCommentManager.save(comment);
			}
			save(issue);
		}
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				for (Pair<Project, String> attachmentInfo: attachmentInfos)
					attachmentStorageManager.moveGroupDir(attachmentInfo.getFirst(), targetProject, attachmentInfo.getSecond());
			}
			
		});
	}
	
	@Transactional
	@Override
	public void saveDescription(Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equal(description, prevDescription)) {
			issue.setDescription(description);
			entityReferenceManager.addReferenceChange(issue, description);
			save(issue);
		}
	}
	
	@Transactional
	@Override
	public void delete(Collection<Issue> issues) {
		for (Issue issue: issues)
			delete(issue);
	}
	
	@Transactional
	@Override
	public void clearSchedules(Project project, Collection<Milestone> milestones) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueSchedule> criteriaQuery = builder.createQuery(IssueSchedule.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);

		List<Predicate> milestonePredicates = new ArrayList<>();
		for (Milestone milestone: milestones) 
			milestonePredicates.add(builder.equal(root.get(IssueSchedule.PROP_MILESTONE), milestone));
		
		Join<Project, Project> join = root
				.join(IssueSchedule.PROP_ISSUE, JoinType.INNER)
				.join(Issue.PROP_PROJECT, JoinType.INNER);
		criteriaQuery.where(builder.and(
				projectManager.getTreePredicate(builder, join, project),
				builder.or(milestonePredicates.toArray(new Predicate[0]))));
		
		for (IssueSchedule schedule: getSession().createQuery(criteriaQuery).getResultList()) 
			dao.remove(schedule);
	}

	@Sessional
	@Override
	public List<Issue> queryAfter(Long afterIssueId, int count) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.addOrder(Order.asc("id"));
		if (afterIssueId != null) 
			criteria.add(Restrictions.gt("id", afterIssueId));
		return query(criteria, 0, count);
	}
	
}
