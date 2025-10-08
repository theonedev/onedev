package io.onedev.server.service.impl;

import static io.onedev.server.model.Issue.PROP_OWN_ESTIMATED_TIME;
import static io.onedev.server.model.Issue.PROP_OWN_SPENT_TIME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.shiro.subject.Subject;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.service.IssueAuthorizationService;
import io.onedev.server.service.IssueFieldService;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueQueryPersonalizationService;
import io.onedev.server.service.IssueScheduleService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueTouchService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.issue.IssueDeleted;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.project.issue.IssuesCopied;
import io.onedev.server.event.project.issue.IssuesDeleted;
import io.onedev.server.event.project.issue.IssuesMoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.changedata.IssueProjectChangeData;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.SequenceGenerator;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.IssueTimes;
import io.onedev.server.util.IterationAndIssueState;
import io.onedev.server.util.ProjectIssueStateStat;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueService extends BaseEntityService<Issue> implements IssueService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueService.class);
    
	@Inject
	private IssueFieldService fieldService;

	@Inject
	private ListenerRegistry listenerRegistry;
	
	@Inject
	private IssueQueryPersonalizationService queryPersonalizationService;
	
	@Inject
	private SettingService settingService;
	
	@Inject
	private ProjectService projectService;
	
	@Inject
	private UserService userService;
	
	@Inject
	private IssueAuthorizationService authorizationService;
	
	@Inject
	private TransactionService transactionService;
	
	@Inject
	private RoleService roleService;
	
	@Inject
	private LinkSpecService linkSpecService;
	
	@Inject
	private IssueLinkService linkService;
	
	@Inject
	private IssueScheduleService scheduleService;
	
	@Inject
	private ClusterService clusterService;
	
	@Inject
	private IssueTouchService touchService;
	
	private SequenceGenerator numberGenerator;
	
	private volatile IMap<String, Long> idCache;

	private synchronized SequenceGenerator getNumberGenerator() {
		if (numberGenerator == null) 
			numberGenerator = new SequenceGenerator(Issue.class, clusterService, dao);
		return numberGenerator;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueService.class);
	}
	
	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarting event) {
		logger.info("Caching issue info...");

		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
        idCache = hazelcastInstance.getMap("issueIds");
        
		var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("issueCacheInited");
		clusterService.initWithLead(cacheInited, () -> {
			Query<?> query = dao.getSession().createQuery("select id, project.id, number from Issue");
			for (Object[] fields: (List<Object[]>)query.list()) {
				Long issueId = (Long) fields[0];
				Long projectId = (Long)fields[1];
				Long issueNumber = (Long) fields[2];
				idCache.put(getCacheKey(projectId, issueNumber), issueId);
			}
			return 1L;
		});
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
	public Issue find(String uuid) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.eq(Issue.PROP_UUID, uuid));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Override
	public void open(Issue issue) {
		open(issue, new ArrayList<>());
	}
	
	@Transactional
	@Override
	public void open(Issue issue, Collection<String> notifiedEmailAddresses) {
		Preconditions.checkArgument(issue.isNew());
		issue.setNumberScope(issue.getProject().getForkRoot());
		issue.setNumber(getNextNumber(issue.getNumberScope()));
		issue.setTotalEstimatedTime(issue.getOwnEstimatedTime());
		issue.setSubmitDate(new Date());
		
		LastActivity lastActivity = new LastActivity();
		lastActivity.setUser(issue.getSubmitter());
		lastActivity.setDescription("opened");
		lastActivity.setDate(issue.getSubmitDate());
		issue.setLastActivity(lastActivity);

		issue.addMissingFields(settingService.getIssueSetting().getPromptFieldsUponIssueOpen(issue.getProject()));
		issue.validateFields();

		dao.persist(issue);
		
		fieldService.saveFields(issue);
		for (IssueSchedule schedule: issue.getSchedules())
			dao.persist(schedule);
		
		if (!SecurityUtils.isAdministrator(issue.getSubmitter().asSubject())) {
			IssueAuthorization authorization = new IssueAuthorization();
			authorization.setIssue(issue);
			authorization.setUser(issue.getSubmitter());
			issue.getAuthorizations().add(authorization);
			authorizationService.createOrUpdate(authorization);
		}
		
		listenerRegistry.post(new IssueOpened(issue, notifiedEmailAddresses));
	}

	@Transactional
	@Override
	public void togglePin(Issue issue) {
		if (issue.getPinDate() != null)
			issue.setPinDate(null);
		else 
			issue.setPinDate(new Date());
		dao.persist(issue);
	}

	private javax.persistence.criteria.Order getOrder(EntitySort sort, CriteriaBuilder builder, From<Issue, Issue> issue) {
		if (Issue.SORT_FIELDS.containsKey(sort.getField())) {
			if (sort.getDirection() == Direction.ASCENDING)
				return builder.asc(IssueQuery.getPath(issue, Issue.SORT_FIELDS.get(sort.getField()).getProperty()));
			else
				return builder.desc(IssueQuery.getPath(issue, Issue.SORT_FIELDS.get(sort.getField()).getProperty()));
		} else {
			Join<Issue, IssueField> join = issue.join(Issue.PROP_FIELDS, JoinType.LEFT);
			join.on(builder.equal(join.get(IssueField.PROP_NAME), sort.getField()));
			if (sort.getDirection() == Direction.ASCENDING)
				return builder.asc(join.get(IssueField.PROP_ORDINAL));
			else
				return builder.desc(join.get(IssueField.PROP_ORDINAL));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<javax.persistence.criteria.Order> buildOrders(EntityQuery<Issue> query, CriteriaBuilder builder, 
															  From<Issue, Issue> issue, 
															  List<javax.persistence.criteria.Order> preferOrders) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();

		for (EntitySort sort: query.getSorts()) 
			orders.add(getOrder(sort, builder, issue));

		orders.addAll(preferOrders);
		
		for (EntitySort sort: query.getBaseSorts()) 
			orders.add(getOrder(sort, builder, issue));
				
		var found = false;
		for (var order: orders) {
			if (order.getExpression() instanceof SingularAttributePath) {
				var expr = (SingularAttributePath) order.getExpression();
				if (expr.getAttribute().getName().equals(LastActivity.PROP_DATE) 
						&& expr.getPathSource() instanceof SingularAttributePath 
						&& ((SingularAttributePath) expr.getPathSource()).getAttribute().getName().equals(Issue.PROP_LAST_ACTIVITY)) {
					found = true;
					break;
				}
			}
		}
		if (!found)
			orders.add(builder.desc(IssueQuery.getPath(issue, Issue.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE)));
		
		return orders;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return settingService.getIssueSetting();
	}

	@Sessional
	@Override
	public List<Issue> query(Subject subject, ProjectScope projectScope, EntityQuery<Issue> issueQuery, 
			boolean loadExtraInfo, int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> criteriaQuery = builder.createQuery(Issue.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		criteriaQuery.where(buildPredicates(subject, projectScope, issueQuery.getCriteria(), criteriaQuery, builder, root));
		var criteria = issueQuery.getCriteria();
		List<javax.persistence.criteria.Order> preferOrders = new ArrayList<>();
		if (criteria != null)
			preferOrders.addAll(criteria.getPreferOrders(builder, root));
		criteriaQuery.orderBy(buildOrders(issueQuery, builder, root, preferOrders));
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (loadExtraInfo && !issues.isEmpty()) {
			fieldService.populateFields(issues);
			linkService.populateLinks(issues);
			scheduleService.populateSchedules(issues);
		}
		return issues;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		if (!(event instanceof IssueOpened || event.isMinor()))
			event.getIssue().setLastActivity(event.getLastUpdate());
	}
	
	@Sessional
	@Override
	public int count(Subject subject, ProjectScope projectScope, Criteria<Issue> issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		criteriaQuery.where(buildPredicates(subject, projectScope, issueCriteria, criteriaQuery, builder, root));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public IssueTimes queryTimes(Subject subject, ProjectScope projectScope, Criteria<Issue> issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueTimes> criteriaQuery = builder.createQuery(IssueTimes.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		criteriaQuery.where(buildPredicates(subject, projectScope, issueCriteria, criteriaQuery, builder, root));
		
		criteriaQuery.multiselect(
				builder.sum(root.get(PROP_OWN_ESTIMATED_TIME)), 
				builder.sum(root.get(PROP_OWN_SPENT_TIME)));
		return getSession().createQuery(criteriaQuery).uniqueResult();
	}
	
	@Override
	public Predicate[] buildPredicates(Subject subject, ProjectScope projectScope, Criteria<Issue> issueCriteria,
									   CriteriaQuery<?> query, CriteriaBuilder builder, From<Issue, Issue> issue) {
		List<Predicate> predicates = new ArrayList<>();
		if (projectScope != null) {
			Project project = projectScope.getProject();
			Path<Project> projectPath = issue.get(Issue.PROP_PROJECT);
			List<Predicate> projectPredicates = new ArrayList<>();
			if (projectScope.isRecursive()) {
				Collection<Long> subtreeIds = projectService.getSubtreeIds(project.getId());
				projectPredicates.add(buildPredicate(subject, builder, issue, subtreeIds));
				projectPredicates.add(buildAuthorizationPredicate(subject, query, builder, issue, subtreeIds));
			} else if (SecurityUtils.canAccessConfidentialIssues(subject, project)) {
				projectPredicates.add(builder.equal(projectPath, project));
			} else {
				projectPredicates.add(buildNonConfidentialPredicate(builder, issue, project));
				projectPredicates.add(buildAuthorizationPredicate(subject, query, builder, issue, project));
			}
			if (projectScope.isInherited()) {
				for (Project ancestor: projectScope.getProject().getAncestors()) {
					if (SecurityUtils.canAccessConfidentialIssues(subject, ancestor)) {
						projectPredicates.add(builder.equal(projectPath, ancestor));
					} else if (SecurityUtils.canAccessProject(subject, ancestor)) { 
						projectPredicates.add(buildNonConfidentialPredicate(builder, issue, ancestor));
						projectPredicates.add(buildAuthorizationPredicate(subject, query, builder, issue, ancestor));
					}
				}
			}
			predicates.add(builder.or(projectPredicates.toArray(new Predicate[0])));
		} else if (!SecurityUtils.isAdministrator(subject)) {
			var projects = SecurityUtils.getAuthorizedProjects(subject, new AccessProject());				
			if (!projects.isEmpty()) { 
				var projectIds = projects.stream().map(AbstractEntity::getId).collect(toSet());
				predicates.add(builder.or(
						buildPredicate(subject, builder, issue, projectIds), 
						buildAuthorizationPredicate(subject, query, builder, issue, projectIds)));
			} else { 
				predicates.add(builder.disjunction());
			}
		}
		if (issueCriteria != null)
			predicates.add(issueCriteria.getPredicate(projectScope, query, issue, builder));

		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private Predicate buildNonConfidentialPredicate(CriteriaBuilder builder, From<Issue, Issue> issue, Project project) {
		return builder.and(
				builder.equal(issue.get(Issue.PROP_PROJECT), project),
				builder.equal(issue.get(Issue.PROP_CONFIDENTIAL), false));
	}
	
	private Predicate buildAuthorizationPredicate(Subject subject, CriteriaQuery<?> query, CriteriaBuilder builder,
												  From<Issue, Issue> issue, Collection<Long> projectIds) {
		User user = SecurityUtils.getUser(subject);
		if (user != null) {
			Subquery<IssueAuthorization> authorizationQuery = query.subquery(IssueAuthorization.class);
			Root<IssueAuthorization> authorizationRoot = authorizationQuery.from(IssueAuthorization.class);
			authorizationQuery.select(authorizationRoot);
	
			Predicate issuePredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_ISSUE), issue);
			Predicate userPredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_USER), user);
			Path<Long> projectIdPath = issue.get(Issue.PROP_PROJECT).get(Project.PROP_ID);
			return builder.and(
					Criteria.forManyValues(builder, projectIdPath, projectIds, projectService.getIds()), 
					builder.exists(authorizationQuery.where(issuePredicate, userPredicate))); 
		} else {
			return builder.disjunction();
		}
	}
	
	private Predicate buildAuthorizationPredicate(Subject subject, CriteriaQuery<?> query, CriteriaBuilder builder,
												  From<Issue, Issue> issue, Project project) {
		User user = SecurityUtils.getUser(subject);
		if (user != null) {
			Subquery<IssueAuthorization> authorizationQuery = query.subquery(IssueAuthorization.class);
			Root<IssueAuthorization> authorizationRoot = authorizationQuery.from(IssueAuthorization.class);
			authorizationQuery.select(authorizationRoot);
	
			Predicate issuePredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_ISSUE), issue);
			Predicate userPredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_USER), user);
			Predicate projectPredicate = builder.equal(issue.get(Issue.PROP_PROJECT), project);
			return builder.and(projectPredicate, builder.exists(authorizationQuery.where(issuePredicate, userPredicate))); 
		} else {
			return builder.disjunction();
		}
	}
	
	private Predicate buildPredicate(Subject subject, CriteriaBuilder builder, From<Issue, Issue> issue, Collection<Long> projectIds) {
		var cache = projectService.cloneCache();
		Collection<Long> allIds = cache.keySet();
		Path<Project> projectPath = issue.get(Issue.PROP_PROJECT);
		Path<Long> projectIdPath = projectPath.get(Project.PROP_ID);
		if (SecurityUtils.isAdministrator(subject)) {
			return Criteria.forManyValues(builder, projectIdPath, projectIds, allIds);
		} else {
			Collection<Long> projectIdsWithConfidentialIssuePermission = new ArrayList<>();
			Collection<Long> projectIdsWithoutConfidentialIssuePermission = new ArrayList<>();
			if (projectIds.size() > 500) {
				var authorizedProjectIds = SecurityUtils.getAuthorizedProjects(subject, cache, new AccessConfidentialIssues()).stream().map(AbstractEntity::getId).collect(toSet());								
				for (Long projectId: projectIds) {
					if (authorizedProjectIds.contains(projectId)) 
						projectIdsWithConfidentialIssuePermission.add(projectId);
					else
						projectIdsWithoutConfidentialIssuePermission.add(projectId);
				}
			} else {
				for (Long projectId: projectIds) {
					Project project = projectService.load(projectId);
					if (SecurityUtils.canAccessConfidentialIssues(subject, project)) 
						projectIdsWithConfidentialIssuePermission.add(projectId);
					else
						projectIdsWithoutConfidentialIssuePermission.add(projectId);
				}
			}
			List<Predicate> predicates = new ArrayList<>(); 
			if (!projectIdsWithConfidentialIssuePermission.isEmpty()) {
				predicates.add(Criteria.forManyValues(
						builder, projectIdPath, projectIdsWithConfidentialIssuePermission, allIds));
			}
			if (!projectIdsWithoutConfidentialIssuePermission.isEmpty()) {
				predicates.add(builder.and(
						Criteria.forManyValues(
								builder, projectIdPath, projectIdsWithoutConfidentialIssuePermission, allIds),
						builder.equal(issue.get(Issue.PROP_CONFIDENTIAL), false)));
			}
			return builder.or(predicates.toArray(new Predicate[0]));
		}
		
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

		query = getSession().createQuery("select distinct state from IssueStateHistory");
		for (String state: query.getResultList()) {
			if (getIssueSetting().getStateSpec(state) == null)
				undefinedStates.add(state);
		}
		
		for (Project project: projectService.query()) {
			undefinedStates.addAll(project.getIssueSetting().getUndefinedStates());
			undefinedStates.addAll(project.getBuildSetting().getUndefinedStates());
		}
		
		for (LinkSpec link: linkSpecService.query()) { 
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				undefinedStates.addAll(updater.getUndefinedStates());
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationService.query()) 
			populateUndefinedStates(undefinedStates, setting.getQueries());
		
		for (User user: userService.query()) 
			populateUndefinedStates(undefinedStates, user.getIssueQueryPersonalization().getQueries());
		
		return undefinedStates;
	}
	
	private void populateUndefinedStates(Collection<String> undefinedStates, List<NamedIssueQuery> namedQueries) {
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				undefinedStates.addAll(IssueQuery.parse(null, namedQuery.getQuery(), option, false).getUndefinedStates());
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(settingService.getUndefinedIssueFields());
		undefinedFields.addAll(roleService.getUndefinedIssueFields());
		
		Query<String> query = getSession().createQuery("select distinct name from IssueField");
		for (String fieldName: query.getResultList()) {
			FieldSpec field = getIssueSetting().getFieldSpec(fieldName);
			if (field == null)
				undefinedFields.add(fieldName);
		}

		for (Project project: projectService.query()) { 
			undefinedFields.addAll(project.getIssueSetting().getUndefinedFields());
			undefinedFields.addAll(project.getBuildSetting().getUndefinedFields());
		}
		
		for (LinkSpec link: linkSpecService.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				undefinedFields.addAll(updater.getUndefinedFields());
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationService.query()) 
			populateUndefinedFields(undefinedFields, setting.getQueries());
		
		for (User user: userService.query()) 
			populateUndefinedFields(undefinedFields, user.getIssueQueryPersonalization().getQueries());
		
		return undefinedFields;
	}
	
	private void populateUndefinedFields(Collection<String> undefinedFields, List<NamedIssueQuery> namedQueries) {
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				undefinedFields.addAll(IssueQuery.parse(null, namedQuery.getQuery(), option, false).getUndefinedFields());
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		
		undefinedFieldValues.addAll(settingService.getUndefinedIssueFieldValues());
		
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

		for (Project project: projectService.query()) {
			undefinedFieldValues.addAll(project.getIssueSetting().getUndefinedFieldValues());
			undefinedFieldValues.addAll(project.getBuildSetting().getUndefinedFieldValues());
		}
		
		for (LinkSpec link: linkSpecService.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				undefinedFieldValues.addAll(updater.getUndefinedFieldValues());
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationService.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, setting.getQueries());
		
		for (User user: userService.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, user.getIssueQueryPersonalization().getQueries());
		
		return undefinedFieldValues;
	}
	
	private void populateUndefinedFieldValues(Collection<UndefinedFieldValue> undefinedFieldValues, 
			List<NamedIssueQuery> namedQueries) {
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				undefinedFieldValues.addAll(IssueQuery.parse(null, namedQuery.getQuery(), option, false).getUndefinedFieldValues());
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
				Query<?> query = getSession().createQuery("update Issue set state=:newState, stateOrdinal=:newStateOrdinal where state=:oldState");
				query.setParameter("oldState", entry.getKey());
				query.setParameter("newState", entry.getValue().getNewState());
				query.setParameter("newStateOrdinal", getIssueSetting().getStateOrdinal(entry.getValue().getNewState()));
				query.executeUpdate();

				query = getSession().createQuery("update IssueStateHistory set state=:newState where state=:oldState");
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

				query = getSession().createQuery("delete from IssueAuthorization where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();

				query = getSession().createQuery("delete from IssueStateHistory where issue in (select issue from Issue issue where issue.state=:state)");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from Issue where state=:state");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();

				query = getSession().createQuery("delete from IssueStateHistory where state=:state");
				query.setParameter("state", entry.getKey());
				query.executeUpdate();
			}
		}
		
		for (Project project: projectService.query()) { 
			project.getIssueSetting().fixUndefinedStates(resolutions);
			project.getBuildSetting().fixUndefinedStates(resolutions);
		}
		
		for (LinkSpec link: linkSpecService.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				updater.fixUndefinedStates(resolutions);
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationService.query()) 
			fixUndefinedStates(resolutions, setting.getQueries());

		for (User user: userService.query())
			fixUndefinedStates(resolutions, user.getIssueQueryPersonalization().getQueries());
	}
	
	private void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions, 
			List<NamedIssueQuery> namedQueries) {
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
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
		roleService.fixUndefinedIssueFields(resolutions);
		
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
		
		for (Project project: projectService.query()) { 
			project.getIssueSetting().fixUndefinedFields(resolutions);
			project.getBuildSetting().fixUndefinedFields(resolutions);
		}
		
		for (LinkSpec link: linkSpecService.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				updater.fixUndefinedFields(resolutions);
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationService.query())
			fixUndefinedFields(resolutions, setting.getProject(), setting.getQueries());
		
		for (User user: userService.query()) 
			fixUndefinedFields(resolutions, null, user.getIssueQueryPersonalization().getQueries());
		
		Map<String, UndefinedFieldResolution> derivedDeletions = 
				getFieldResolutions(settingService.fixUndefinedIssueFields(resolutions));
		if (!derivedDeletions.isEmpty())
			fixUndefinedFields(derivedDeletions);
	}
	
	private void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(project, namedQuery.getQuery(), option, false);
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
		
		for (Project project: projectService.query()) {
			project.getIssueSetting().fixUndefinedFieldValues(resolutions);
			project.getBuildSetting().fixUndefinedFieldValues(resolutions);
		}
		
		for (LinkSpec link: linkSpecService.query()) { 
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				updater.fixUndefinedFieldValues(resolutions);
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationService.query()) 
			fixUndefinedFieldValues(resolutions, setting.getProject(), setting.getQueries());
		
		for (User user: userService.query())
			fixUndefinedFieldValues(resolutions, null, user.getIssueQueryPersonalization().getQueries());
		
		Map<String, UndefinedFieldResolution> derivedDeletions = 
				getFieldResolutions(settingService.fixUndefinedIssueFieldValues(resolutions));
		if (!derivedDeletions.isEmpty())
			fixUndefinedFields(derivedDeletions);
	}
	
	private void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), option, false);
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
	public void fixStateAndFieldOrdinals() {
		int stateOrdinal = 0;
		for (StateSpec state: getIssueSetting().getStateSpecs()) {
			Query query = getSession().createQuery("update Issue set stateOrdinal=:stateOrdinal "
					+ "where state=:state and stateOrdinal!=:stateOrdinal");
			query.setParameter("state", state.getName());
			query.setParameter("stateOrdinal", stateOrdinal);
			query.executeUpdate();
			stateOrdinal++;
		}
		
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

	
	@Transactional
	@Override
	public void delete(Issue issue) {
		dao.remove(issue);
		listenerRegistry.post(new IssueDeleted(issue));
	}
	
	private String getCacheKey(Issue issue) {
		return getCacheKey(issue.getProject().getId(), issue.getNumber());
	}
	
	private String getCacheKey(Long projectId, Long issueNumber) {
		return projectId + ":" + issueNumber;
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			var issueKey = getCacheKey(issue);
			var issueId = issue.getId();
			transactionService.runAfterCommit(() -> idCache.put(issueKey, issueId));
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			var cacheKey = getCacheKey((Issue) event.getEntity());
			transactionService.runAfterCommit(() -> idCache.remove(cacheKey));
			
		} else if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	if (project.getForkRoot().equals(project))
	    		getNumberGenerator().removeNextSequence(project);
			
			Long projectId = project.getId();
			transactionService.runAfterCommit(() -> idCache.removeAll(entry -> entry.getKey().startsWith(projectId + ":")));
		}
	}
	
	private Long getIssueId(Long projectId, Long issueNumber) {
		return idCache.get(getCacheKey(projectId, issueNumber));
	}
	
	@Sessional
	@Override
	public Collection<IterationAndIssueState> queryIterationAndIssueStates(Project project, Collection<Iteration> iterations) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IterationAndIssueState> criteriaQuery = builder.createQuery(IterationAndIssueState.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);
		Join<Issue, Issue> issueJoin = root.join(IssueSchedule.PROP_ISSUE, JoinType.INNER);
		criteriaQuery.multiselect(
				root.get(IssueSchedule.PROP_ITERATION).get(Iteration.PROP_ID), 
				issueJoin.get(Issue.PROP_STATE));
		
		List<Predicate> iterationPredicates = new ArrayList<>();
		for (Iteration iteration: iterations) 
			iterationPredicates.add(builder.equal(root.get(IssueSchedule.PROP_ITERATION), iteration));
		
		criteriaQuery.where(builder.and(
				buildSubtreePredicate(builder, issueJoin.get(Issue.PROP_PROJECT), project),
				builder.or(iterationPredicates.toArray(new Predicate[0]))));
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}
	
	@Sessional
	@Override
	public Collection<Iteration> queryUsedIterations(Project project) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Iteration> criteriaQuery = builder.createQuery(Iteration.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);
		criteriaQuery.select(root.get(IssueSchedule.PROP_ITERATION));
		
		Path<Project> projectPath = root
				.join(IssueSchedule.PROP_ISSUE, JoinType.INNER)
				.get(Issue.PROP_PROJECT);
		
		criteriaQuery.where(buildSubtreePredicate(builder, projectPath, project));
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}
	
	private Predicate buildSubtreePredicate(CriteriaBuilder builder, Path<Project> projectPath, Project project) {
		Collection<Long> subtreeIds = projectService.getSubtreeIds(project.getId());
		Collection<Long> allIds = projectService.getIds();
		return Criteria.forManyValues(builder, projectPath.get(Project.PROP_ID), subtreeIds, allIds);
	}
	
	@Transactional
	@Override
	public void copy(Collection<Issue> issues, Project sourceProject, Project targetProject) {
		List<Issue> sortedIssues = new ArrayList<>(issues);
		Collections.sort(sortedIssues);
		Map<Issue, Issue> cloneMapping = new HashMap<>();
		Map<Long, Long> numberMapping = new HashMap<>();

		sortedIssues.forEach(issue -> {
			Issue clonedIssue = VersionedXmlDoc.cloneBean(issue);
			clonedIssue.setId(null);
			clonedIssue.setUUID(UUID.randomUUID().toString());
			clonedIssue.setProject(targetProject);
			Project numberScope = targetProject.getForkRoot();
			
			clonedIssue.setNumberScope(numberScope);
			clonedIssue.setNumber(getNextNumber(numberScope));
			cloneMapping.put(issue, clonedIssue);
			numberMapping.put(issue.getNumber(), clonedIssue.getNumber());
		});

		cloneMapping.forEach((key, value) -> {
			var description = value.getDescription();
			if (description != null) {
				description = description.replace(
						key.getAttachmentProject().getId() + "/attachments/" + key.getAttachmentGroup(),
						value.getAttachmentProject().getId() + "/attachments/" + value.getAttachmentGroup());
				description = new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(description, "#");
				value.setDescription(description);
			}
			dao.persist(value);
			
			key.getComments().forEach(comment -> {
				var clonedComment = VersionedXmlDoc.cloneBean(comment);
				clonedComment.setId(null);
				clonedComment.setIssue(value);
				String content = clonedComment.getContent();
				content = content.replace(
						key.getAttachmentProject().getId() + "/attachments/" + key.getAttachmentGroup(),
						value.getAttachmentProject().getId() + "/attachments/" + value.getAttachmentGroup());
				content = new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(content, "#");
				clonedComment.setContent(content);
				dao.persist(clonedComment);
			});

			key.getFields().forEach(field -> {
				var clonedField = VersionedXmlDoc.cloneBean(field);
				clonedField.setId(null);
				clonedField.setIssue(value);
				dao.persist(clonedField);
			});

		});

		var processedLinks = new HashSet<>();
		cloneMapping.forEach((key, value) -> {
			key.getSourceLinks().forEach(link -> {
				if (processedLinks.add(link)) {
					var clonedSource = cloneMapping.get(link.getSource());
					if (clonedSource != null) {
						var clonedLink = VersionedXmlDoc.cloneBean(link);
						clonedLink.setId(null);
						clonedLink.setSource(clonedSource);
						clonedLink.setTarget(value);
						dao.persist(clonedLink);
					}
				}
			});
			key.getTargetLinks().forEach(link -> {
				if (processedLinks.add(link)) {
					var clonedTarget = cloneMapping.get(link.getTarget());
					if (clonedTarget != null) {
						var clonedLink = VersionedXmlDoc.cloneBean(link);
						clonedLink.setId(null);
						clonedLink.setTarget(clonedTarget);
						clonedLink.setSource(value);
						dao.persist(clonedLink);
					}
				}
			});
		});
		
		listenerRegistry.post(new IssuesCopied(sourceProject, targetProject, cloneMapping));
	}
	
	@Transactional
	@Override
	public void move(User user, Collection<Issue> issues, Project sourceProject, Project targetProject) {
		Map<Long, Long> numberMapping = new HashMap<>();
		List<Issue> sortedIssues = new ArrayList<>(issues);
		Collections.sort(sortedIssues);
		for (Issue issue: sortedIssues) {
			if (issue.getDescription() != null) {
				issue.setDescription(issue.getDescription().replace(
						sourceProject.getId() + "/attachments/" + issue.getAttachmentGroup(), 
						targetProject.getId() + "/attachments/" + issue.getAttachmentGroup()));
			}
			for (IssueComment comment: issue.getComments()) {
				comment.setContent(comment.getContent().replace(
						sourceProject.getId() + "/attachments/" + issue.getAttachmentGroup(), 
						targetProject.getId() + "/attachments/" + issue.getAttachmentGroup()));
			}
 
			Project oldProject = issue.getProject();
			Project numberScope = targetProject.getForkRoot();
			Long nextNumber = getNextNumber(numberScope);
			issue.setProject(targetProject);
			issue.setNumberScope(numberScope);
			Long oldNumber = issue.getNumber();
			issue.setNumber(nextNumber);
			numberMapping.put(oldNumber, nextNumber);
			
			for (IssueSchedule schedule: issue.getSchedules()) {
				if (schedule.getIteration() != null 
						&& !schedule.getIteration().getProject().isSelfOrAncestorOf(targetProject)) {
					dao.remove(schedule);
				}
			}

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueProjectChangeData(oldProject.getPath(), targetProject.getPath()));
			dao.persist(change);
		}
		
		for (Issue issue: sortedIssues) {
			if (issue.getDescription() != null) {
				issue.setDescription(new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(issue.getDescription(), "#"));
			}
			
			for (IssueComment comment: issue.getComments()) {
				comment.setContent(new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(comment.getContent(), "#"));
				dao.persist(comment);
			}
			dao.persist(issue);
		}

		touchService.touch(sourceProject, issues.stream().map(Issue::getId).collect(toList()), false);
		
		listenerRegistry.post(new IssuesMoved(sourceProject, targetProject, issues));
	}
	
	@Transactional
	@Override
	public void delete(Collection<Issue> issues, Project project) {
		for (Issue issue: issues)
			dao.remove(issue);
		listenerRegistry.post(new IssuesDeleted(project, issues));
	}
	
	@Transactional
	@Override
	public void clearSchedules(Project project, Collection<Iteration> iterations) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueSchedule> criteriaQuery = builder.createQuery(IssueSchedule.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);

		List<Predicate> iterationPredicates = new ArrayList<>();
		for (Iteration iteration: iterations) 
			iterationPredicates.add(builder.equal(root.get(IssueSchedule.PROP_ITERATION), iteration));
		
		Path<Project> projectPath = root
				.join(IssueSchedule.PROP_ISSUE, JoinType.INNER)
				.get(Issue.PROP_PROJECT);
		criteriaQuery.where(builder.and(
				buildSubtreePredicate(builder, projectPath, project),
				builder.or(iterationPredicates.toArray(new Predicate[0]))));
		
		for (IssueSchedule schedule: getSession().createQuery(criteriaQuery).getResultList()) 
			dao.remove(schedule);
	}

	@Sessional
	@Override
	public List<Issue> queryAfter(Long projectId, Long afterIssueId, int count) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt("id", afterIssueId));
		criteria.addOrder(Order.asc("id"));
		return query(criteria, 0, count);
	}

	@Sessional
	@Override
	public List<ProjectIssueStateStat> queryStateStats(Subject subject, Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectIssueStateStat> criteriaQuery = builder.createQuery(ProjectIssueStateStat.class);
			Root<Issue> root = criteriaQuery.from(Issue.class);
			
			criteriaQuery.multiselect(
					root.get(Issue.PROP_PROJECT).get(Project.PROP_ID), 
					root.get(Issue.PROP_STATE_ORDINAL), 
					builder.count(root));
			criteriaQuery.groupBy(root.get(Issue.PROP_PROJECT), root.get(Issue.PROP_STATE_ORDINAL));
			
			Collection<Project> projectsWithConfidentialIssuePermission = new ArrayList<>();
			Collection<Project> projectsWithoutConfidentialIssuePermission = new ArrayList<>();
			List<Long> projectIds = new ArrayList<>();
			for (Project project: projects) {
				if (SecurityUtils.canAccessConfidentialIssues(subject, project))
					projectsWithConfidentialIssuePermission.add(project);
				else
					projectsWithoutConfidentialIssuePermission.add(project);
				projectIds.add(project.getId());
			}
			
			List<Predicate> predicates = new ArrayList<>();
			if (!projectsWithConfidentialIssuePermission.isEmpty())
				predicates.add(root.get(Issue.PROP_PROJECT).in(projectsWithConfidentialIssuePermission));
			if (!projectsWithoutConfidentialIssuePermission.isEmpty()) {
				predicates.add(builder.and(
						builder.equal(root.get(Issue.PROP_CONFIDENTIAL), false),
						root.get(Issue.PROP_PROJECT).in(projectsWithoutConfidentialIssuePermission)));
			}
			predicates.add(buildAuthorizationPredicate(subject, criteriaQuery, builder, root, projectIds));
			
			criteriaQuery.where(builder.or(predicates.toArray(new Predicate[0])));
			criteriaQuery.orderBy(builder.asc(root.get(Issue.PROP_STATE_ORDINAL)));
			
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	@Override
	public Collection<Long> parseFixedIssueIds(Project project, String commitMessage) {
		Collection<Long> issueIds = new HashSet<>();
		
		for (var reference: getIssueSetting().getCommitMessageFixPatterns().parseFixedIssues(commitMessage, project)) {
			var referenceProject = reference.getProject();
			if (referenceProject.isSelfOrAncestorOf(project) || project.isSelfOrAncestorOf(referenceProject)) {
				Long issueId = getIssueId(referenceProject.getId(), reference.getNumber());
				if (issueId != null)
					issueIds.add(issueId);
			}
		}
		return issueIds;
	}

	@Override
	public Long getNextNumber(Project numberScope) {
		return getNumberGenerator().getNextSequence(numberScope);
	}

	@Override
	public void resetNextNumber(Project numberScope) {
		getNumberGenerator().removeNextSequence(numberScope);
	}

	@Sessional
	@Override
	public List<Issue> queryPinned(Subject subject, Project project) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> criteriaQuery = builder.createQuery(Issue.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		criteriaQuery.where(
				builder.equal(root.get(Issue.PROP_PROJECT), project), 
				builder.isNotNull(root.get(Issue.PROP_PIN_DATE)));
		criteriaQuery.orderBy(builder.desc(root.get(Issue.PROP_PIN_DATE)));

		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(Integer.MAX_VALUE);
		
		var issues = query.getResultList();
		for (var it = issues.iterator(); it.hasNext();) {
			if (!SecurityUtils.canAccessIssue(subject, it.next()))
				it.remove();
		}
		return issues;
	}

	@Sessional
	@Override
	public List<Issue> loadIssues(List<Long> issueIds) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.in("id", issueIds));
		var issues = query(criteria);

		Map<Long, Issue> issueMap = new HashMap<>();
		for (Issue issue: issues)
			issueMap.put(issue.getId(), issue);
		
		List<Issue> orderedIssues = new ArrayList<>();
		for (Long issueId: issueIds) {
			Issue issue = issueMap.get(issueId);
			if (issue != null)
				orderedIssues.add(issue);
		}
		return orderedIssues;
	}

	@Sessional
	@Override
	public List<Issue> query(User submitter, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		From<Issue, Issue> root = query.from(Issue.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(Issue.PROP_SUBMITTER), submitter));
		predicates.add(builder.greaterThanOrEqualTo(root.get(Issue.PROP_SUBMIT_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(Issue.PROP_SUBMIT_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}
	
	@Sessional
	@Override
	public Collection<Long> getProjectIds() {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		criteriaQuery.select(root.get(Issue.PROP_PROJECT).get(Project.PROP_ID));
		criteriaQuery.distinct(true);
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}

}
