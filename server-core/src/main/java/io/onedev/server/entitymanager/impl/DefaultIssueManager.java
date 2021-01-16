package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.changedata.IssueChangeData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
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
import io.onedev.server.util.MilestoneAndState;
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
	
	private final IssueQuerySettingManager issueQuerySettingManager;
	
	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final TransactionManager transactionManager;
	
	private final RoleManager roleManager;
	
	private final Map<Long, IssueFacade> issues = new HashMap<>();
	
	private final ReadWriteLock issuesLock = new ReentrantReadWriteLock();
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager issueFieldManager, 
			TransactionManager transactionManager, IssueQuerySettingManager issueQuerySettingManager, 
			SettingManager settingManager, ListenerRegistry listenerRegistry, 
			ProjectManager projectManager, UserManager userManager, RoleManager roleManager) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
		this.issueQuerySettingManager = issueQuerySettingManager;
		this.listenerRegistry = listenerRegistry;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
		this.userManager = userManager;
		this.roleManager = roleManager;
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching issue info...");
		
		Query<?> query = dao.getSession().createQuery("select id, project.id, number from Issue");
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long issueId = (Long) fields[0];
			issues.put(issueId, new IssueFacade(issueId, (Long)fields[1], (Long)fields[2]));
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
	
	@Sessional
	@Override
	public Issue find(String issueFQN) {
		return find(ProjectScopedNumber.from(issueFQN));
	}
	
	@Sessional
	@Override
	public Issue find(ProjectScopedNumber issueFQN) {
		return find(issueFQN.getProject(), issueFQN.getNumber());
	}
	
	@Transactional
	@Override
	public void open(Issue issue) {
		Preconditions.checkArgument(issue.isNew());
		issue.setNumberScope(issue.getProject().getForkRoot());
		Query<?> query = getSession().createQuery(String.format("select max(%s) from Issue where %s=:numberScope", 
				Issue.PROP_NUMBER, Issue.PROP_NUMBER_SCOPE));
		query.setParameter("numberScope", issue.getNumberScope());
		issue.setNumber(getNextNumber(issue.getNumberScope(), query));
		
		IssueOpened event = new IssueOpened(issue);
		issue.setLastUpdate(event.getLastUpdate());
		
		save(issue);

		issueFieldManager.saveFields(issue);
		
		listenerRegistry.post(event);
	}
	
	@Transactional
	@Override
	public void save(Issue issue) {
		super.save(issue);
		
		IssueFacade facade = issue.getFacade();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				issuesLock.writeLock().lock();
				try {
					issues.put(facade.getId(), facade);
				} finally {
					issuesLock.writeLock().unlock();
				}
			}
			
		});
	}

	private Predicate[] getPredicates(@Nullable Project project, 
			@Nullable io.onedev.server.search.entity.EntityCriteria<Issue> criteria, Root<Issue> root, 
			CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (project != null) {
			predicates.add(builder.equal(root.get(Issue.PROP_PROJECT), project));
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new AccessProject()); 
			if (!projects.isEmpty())
				predicates.add(root.get(Issue.PROP_PROJECT).in(projects));
			else
				predicates.add(builder.disjunction());
		}
		if (criteria != null)
			predicates.add(criteria.getPredicate(root, builder));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Issue> buildCriteriaQuery(@Nullable Project project, 
			Session session, EntityQuery<Issue> issueQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);
		
		query.where(getPredicates(project, issueQuery.getCriteria(), root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: issueQuery.getSorts()) {
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
		query.orderBy(orders);
		
		return query;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return settingManager.getIssueSetting();
	}

	@Sessional
	@Override
	public List<Issue> query(@Nullable Project project, EntityQuery<Issue> issueQuery, 
			int firstResult, int maxResults, boolean loadFields) {
		CriteriaQuery<Issue> criteriaQuery = buildCriteriaQuery(project, getSession(), issueQuery);
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
			if (changeData instanceof IssueReferencedFromCodeCommentData
					|| changeData instanceof IssueReferencedFromIssueData
					|| changeData instanceof IssueReferencedFromPullRequestData) {
				minorChange = true;
			}
		}

		if (!(event instanceof IssueOpened || minorChange))
			event.getIssue().setLastUpdate(event.getLastUpdate());
	}
	
	@Sessional
	@Override
	public int count(@Nullable Project project,IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		criteriaQuery.where(getPredicates(project, issueCriteria, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
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
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			populateUndefinedStates(undefinedStates, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query()) 
			populateUndefinedStates(undefinedStates, null, user.getIssueQuerySetting().getUserQueries());

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
		Collection<String> undefinedFields = getIssueSetting().getUndefinedFields();
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
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			populateUndefinedFields(undefinedFields, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query()) 
			populateUndefinedFields(undefinedFields, null, user.getIssueQuerySetting().getUserQueries());
		
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
		Collection<UndefinedFieldValue> undefinedFieldValues = getIssueSetting().getUndefinedFieldValues();
		
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
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, null, user.getIssueQuerySetting().getUserQueries());
		
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
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			fixUndefinedStates(setting.getProject(), resolutions, setting.getUserQueries());

		for (User user: userManager.query())
			fixUndefinedStates(null, resolutions, user.getIssueQuerySetting().getUserQueries());
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
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query())
			fixUndefinedFields(resolutions, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query()) 
			fixUndefinedFields(resolutions, null, user.getIssueQuerySetting().getUserQueries());
		
		Map<String, UndefinedFieldResolution> derivedDeletions = 
				getFieldResolutions(getIssueSetting().fixUndefinedFields(resolutions));
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
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			fixUndefinedFieldValues(resolutions, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query())
			fixUndefinedFieldValues(resolutions, null, user.getIssueQuerySetting().getUserQueries());
		
		Map<String, UndefinedFieldResolution> derivedDeletions = 
				getFieldResolutions(getIssueSetting().fixUndefinedFieldValues(resolutions));
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
	public List<Issue> query(Project project, String term, int count) {
		EntityCriteria<Issue> criteria = newCriteria();
		
		Set<Project> projects = Sets.newHashSet(project);
		projects.addAll(project.getForkParents().stream().filter(it->SecurityUtils.canAccess(it)).collect(Collectors.toSet()));
		criteria.add(Restrictions.in(Issue.PROP_PROJECT, projects));
		
		if (term.startsWith("#"))
			term = term.substring(1);
		if (term.length() != 0) {
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq(Issue.PROP_NUMBER, buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike(Issue.PROP_TITLE, term, MatchMode.ANYWHERE),
						Restrictions.ilike(Issue.PROP_NO_SPACE_TITLE, term, MatchMode.ANYWHERE)));
			}
		}

		criteria.addOrder(Order.desc(Issue.PROP_PROJECT));
		criteria.addOrder(Order.desc(Issue.PROP_NUMBER));
		return query(criteria, 0, count);
	}
	
	@Transactional
	@Override
	public void delete(Issue issue) {
		super.delete(issue);
		
		Long issueId = issue.getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				issuesLock.writeLock().lock();
				try {
					issues.remove(issueId);
				} finally {
					issuesLock.writeLock().unlock();
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
					issuesLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, IssueFacade>> it = issues.entrySet().iterator(); it.hasNext();) {
							IssueFacade issue = it.next().getValue();
							if (issue.getProjectId().equals(projectId))
								it.remove();
						}
					} finally {
						issuesLock.writeLock().unlock();
					}
				}
			});
		}
	}

	@Override
	public Collection<Long> getIssueNumbers(Long projectId) {
		issuesLock.readLock().lock();
		try {
			Collection<Long> issueNumbers = new HashSet<>();
			for (IssueFacade issue: issues.values()) {
				if (projectId.equals(issue.getProjectId()))
					issueNumbers.add(issue.getNumber());
			}
			return issueNumbers;
		} finally {
			issuesLock.readLock().unlock();
		}
	}

	@Sessional
	@Override
	public Collection<MilestoneAndState> queryMilestoneAndStates(Project project, Collection<Milestone> milestones) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<MilestoneAndState> criteriaQuery = builder.createQuery(MilestoneAndState.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		criteriaQuery.multiselect(
				root.get(Issue.PROP_MILESTONE).get(Milestone.PROP_ID), 
				root.get(Issue.PROP_STATE));
		
		List<Predicate> milestonePredicates = new ArrayList<>();
		for (Milestone milestone: milestones) 
			milestonePredicates.add(builder.equal(root.get(Issue.PROP_MILESTONE), milestone));
		
		criteriaQuery.where(builder.and(
				builder.equal(root.get(Issue.PROP_PROJECT), project),
				builder.or(milestonePredicates.toArray(new Predicate[0]))));
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}

}
