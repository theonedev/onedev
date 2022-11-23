package io.onedev.server.entitymanager.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Objects;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hazelcast.core.HazelcastInstance;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.IssueAuthorizationManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.EntityReferenceManager;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.changedata.IssueChangeData;
import io.onedev.server.model.support.issue.changedata.IssueProjectChangeData;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.SequenceGenerator;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.MilestoneAndIssueState;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ProjectIssueStats;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.validation.ProjectPathValidator;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends BaseEntityManager<Issue> implements IssueManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueManager.class);
	
	private static final List<String> ISSUE_FIX_WORDS = Lists.newArrayList(
			"fix", "fixed", "fixes", "fixing", 
			"resolve", "resolved", "resolves", "resolving", 
			"close", "closed", "closes", "closing");
	
    private static final Pattern ISSUE_FIX_PATTERN;
    
    static {
    	StringBuilder builder = new StringBuilder("(^|[\\W|/]+)(");
    	builder.append(StringUtils.join(ISSUE_FIX_WORDS, "|"));
    	builder.append(")\\s+issue\\s+(");
    	builder.append(JavaEscape.unescapeJava(ProjectPathValidator.PATTERN.pattern()));
    	builder.append(")?#(\\d+)(?=$|[\\W|/]+)");
    	ISSUE_FIX_PATTERN = Pattern.compile(builder.toString());
    }
    
	private final IssueFieldManager fieldManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueQueryPersonalizationManager queryPersonalizationManager;
	
	private final AttachmentManager attachmentManager;
	
	private final IssueCommentManager commentManager;
	
	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final IssueAuthorizationManager authorizationManager;
	
	private final TransactionManager transactionManager;
	
	private final EntityReferenceManager entityReferenceManager;
	
	private final RoleManager roleManager;
	
	private final LinkSpecManager linkSpecManager;
	
	private final IssueLinkManager linkManager;
	
	private final ClusterManager clusterManager;
	
	private final IssueChangeManager changeManager;
	
	private final SequenceGenerator numberGenerator;
	
	private volatile Map<ProjectScopedNumber, Long> issueIds;
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager fieldManager, 
			TransactionManager transactionManager, IssueQueryPersonalizationManager queryPersonalizationManager, 
			SettingManager settingManager, ListenerRegistry listenerRegistry, 
			ProjectManager projectManager, UserManager userManager, ClusterManager clusterManager,
			RoleManager roleManager, AttachmentManager attachmentStorageManager, 
			IssueCommentManager commentManager, EntityReferenceManager entityReferenceManager, 
			LinkSpecManager linkSpecManager, IssueLinkManager linkManager, 
			IssueAuthorizationManager authorizationManager, IssueChangeManager changeManager) {
		super(dao);
		this.fieldManager = fieldManager;
		this.queryPersonalizationManager = queryPersonalizationManager;
		this.listenerRegistry = listenerRegistry;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
		this.userManager = userManager;
		this.roleManager = roleManager;
		this.linkSpecManager = linkSpecManager;
		this.linkManager = linkManager;
		this.attachmentManager = attachmentStorageManager;
		this.commentManager = commentManager;
		this.entityReferenceManager = entityReferenceManager;
		this.authorizationManager = authorizationManager;
		this.clusterManager = clusterManager;
		this.changeManager = changeManager;
		
		numberGenerator = new SequenceGenerator(Issue.class, clusterManager, dao);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueManager.class);
	}
	
	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching issue info...");

		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
        issueIds = hazelcastInstance.getReplicatedMap("issueIds");
        
		Query<?> query = dao.getSession().createQuery("select id, project.id, number from Issue");
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long issueId = (Long) fields[0];
			Long projectId = (Long)fields[1];
			Long issueNumber = (Long) fields[2];
			issueIds.put(new ProjectScopedNumber(projectId, issueNumber), issueId);
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
		
		LastUpdate lastUpdate = new LastUpdate();
		lastUpdate.setUser(issue.getSubmitter());
		lastUpdate.setActivity("opened");
		lastUpdate.setDate(issue.getSubmitDate());
		issue.setLastUpdate(lastUpdate);
		
		save(issue);

		fieldManager.saveFields(issue);
		for (IssueSchedule schedule: issue.getSchedules())
			dao.persist(schedule);
		
		IssueAuthorization authorization = new IssueAuthorization();
		authorization.setIssue(issue);
		authorization.setUser(issue.getSubmitter());
		issue.getAuthorizations().add(authorization);
		authorizationManager.save(authorization);
		
		listenerRegistry.post(new IssueOpened(issue));
	}

	@Transactional
	@Override
	public void save(Issue issue) {
		super.save(issue);
		
		Long projectId = issue.getProject().getId();
		Long issueId = issue.getId();
		Long issueNumber = issue.getNumber();
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				issueIds.put(new ProjectScopedNumber(projectId, issueNumber), issueId);
			}
			
		});
	}

	private List<javax.persistence.criteria.Order> getOrders(List<EntitySort> sorts, CriteriaBuilder builder, Root<Issue> root) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: sorts) {
			if (Issue.ORDER_FIELDS.containsKey(sort.getField())) {
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(IssueQuery.getPath(root, Issue.ORDER_FIELDS.get(sort.getField()).getProperty())));
				else
					orders.add(builder.desc(IssueQuery.getPath(root, Issue.ORDER_FIELDS.get(sort.getField()).getProperty())));
			} else {
				Join<Issue, IssueField> join = root.join(Issue.PROP_FIELDS, JoinType.LEFT);
				join.on(builder.equal(join.get(IssueField.PROP_NAME), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get(IssueField.PROP_ORDINAL)));
				else
					orders.add(builder.desc(join.get(IssueField.PROP_ORDINAL)));
			}
		}

		if (orders.isEmpty())
			orders.add(builder.desc(IssueQuery.getPath(root, Issue.PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE)));
		
		return orders;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return settingManager.getIssueSetting();
	}

	@Sessional
	@Override
	public List<Issue> query(@Nullable ProjectScope projectScope, EntityQuery<Issue> issueQuery, 
			boolean loadFieldsAndLinks, int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> criteriaQuery = builder.createQuery(Issue.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		criteriaQuery.where(getPredicates(projectScope, issueQuery.getCriteria(), criteriaQuery, builder, root));
		criteriaQuery.orderBy(getOrders(issueQuery.getSorts(), builder, root));
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (loadFieldsAndLinks && !issues.isEmpty()) {
			fieldManager.populateFields(issues);
			linkManager.populateLinks(issues);
		}
		
		return issues;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		boolean minorChange = false;
		if (event instanceof IssueChanged) {
			IssueChangeData changeData = ((IssueChanged)event).getChange().getData();
			if (changeData instanceof ReferencedFromAware) 
				minorChange = true;
		}

		if (!(event instanceof IssueOpened || minorChange))
			event.getIssue().setLastUpdate(event.getLastUpdate());
	}
	
	@Sessional
	@Override
	public int count(@Nullable ProjectScope projectScope, Criteria<Issue> issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		criteriaQuery.where(getPredicates(projectScope, issueCriteria, criteriaQuery, builder, root));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}
	
	private Predicate[] getPredicates(@Nullable ProjectScope projectScope, @Nullable Criteria<Issue> issueCriteria, 
			CriteriaQuery<?> query, CriteriaBuilder builder, Root<Issue> root) {
		List<Predicate> predicates = new ArrayList<>();
		if (projectScope != null) {
			Project project = projectScope.getProject();
			Path<Project> projectPath = root.get(Issue.PROP_PROJECT);
			List<Predicate> projectPredicates = new ArrayList<>();
			if (projectScope.isRecursive()) {
				Collection<Long> subtreeIds = projectManager.getSubtreeIds(project.getId());
				projectPredicates.add(getPredicate(builder, root, subtreeIds));
				projectPredicates.add(getAuthorizationPredicate(query, builder, root, subtreeIds));
			} else if (SecurityUtils.canAccessConfidentialIssues(project)) {
				projectPredicates.add(builder.equal(projectPath, project));
			} else {
				projectPredicates.add(getNonConfidentialPredicate(builder, root, project));
				projectPredicates.add(getAuthorizationPredicate(query, builder, root, project));
			}
			if (projectScope.isInherited()) {
				for (Project ancestor: projectScope.getProject().getAncestors()) {
					if (SecurityUtils.canAccessConfidentialIssues(ancestor)) {
						projectPredicates.add(builder.equal(projectPath, ancestor));
					} else if (SecurityUtils.canAccess(ancestor)) { 
						projectPredicates.add(getNonConfidentialPredicate(builder, root, ancestor));
						projectPredicates.add(getAuthorizationPredicate(query, builder, root, ancestor));
					}
				}
			}
			predicates.add(builder.or(projectPredicates.toArray(new Predicate[0])));
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new AccessProject()); 
			if (!projects.isEmpty()) { 
				Collection<Long> projectIds = projects.stream().map(it->it.getId()).collect(Collectors.toSet());
				predicates.add(builder.or(
						getPredicate(builder, root, projectIds), 
						getAuthorizationPredicate(query, builder, root, projectIds)));
			} else { 
				predicates.add(builder.disjunction());
			}
		}
		if (issueCriteria != null)
			predicates.add(issueCriteria.getPredicate(query, root, builder));

		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private Predicate getNonConfidentialPredicate(CriteriaBuilder builder, Root<Issue> root, Project project) {
		return builder.and(
				builder.equal(root.get(Issue.PROP_PROJECT), project),
				builder.equal(root.get(Issue.PROP_CONFIDENTIAL), false));
	}
	
	private Predicate getAuthorizationPredicate(CriteriaQuery<?> query, CriteriaBuilder builder, 
			Root<Issue> root, Collection<Long> projectIds) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Subquery<IssueAuthorization> authorizationQuery = query.subquery(IssueAuthorization.class);
			Root<IssueAuthorization> authorizationRoot = authorizationQuery.from(IssueAuthorization.class);
			authorizationQuery.select(authorizationRoot);
	
			Predicate issuePredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_ISSUE), root);
			Predicate userPredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_USER), user);
			Path<Long> projectIdPath = root.get(Issue.PROP_PROJECT).get(Project.PROP_ID);
			return builder.and(
					Criteria.forManyValues(builder, projectIdPath, projectIds, projectManager.getIds()), 
					builder.exists(authorizationQuery.where(issuePredicate, userPredicate))); 
		} else {
			return builder.disjunction();
		}
	}
	
	private Predicate getAuthorizationPredicate(CriteriaQuery<?> query, CriteriaBuilder builder, 
			Root<Issue> root, Project project) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Subquery<IssueAuthorization> authorizationQuery = query.subquery(IssueAuthorization.class);
			Root<IssueAuthorization> authorizationRoot = authorizationQuery.from(IssueAuthorization.class);
			authorizationQuery.select(authorizationRoot);
	
			Predicate issuePredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_ISSUE), root);
			Predicate userPredicate = builder.equal(authorizationRoot.get(IssueAuthorization.PROP_USER), user);
			Predicate projectPredicate = builder.equal(root.get(Issue.PROP_PROJECT), project);
			return builder.and(projectPredicate, builder.exists(authorizationQuery.where(issuePredicate, userPredicate))); 
		} else {
			return builder.disjunction();
		}
	}
	
	private Predicate getPredicate(CriteriaBuilder builder, Root<Issue> root, Collection<Long> projectIds) {
		Collection<Long> allIds = projectManager.getIds();
		Path<Project> projectPath = root.get(Issue.PROP_PROJECT);
		Path<Long> projectIdPath = projectPath.get(Project.PROP_ID);
		if (SecurityUtils.isAdministrator()) {
			return Criteria.forManyValues(builder, projectIdPath, projectIds, allIds);
		} else {
			Collection<Long> projectIdsWithConfidentialIssuePermission = new ArrayList<>();
			Collection<Long> projectIdsWithoutConfidentialIssuePermission = new ArrayList<>();
			for (Long projectId: projectIds) {
				Project project = projectManager.load(projectId);
				if (SecurityUtils.canAccessConfidentialIssues(project)) 
					projectIdsWithConfidentialIssuePermission.add(projectId);
				else
					projectIdsWithoutConfidentialIssuePermission.add(projectId);
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
						builder.equal(root.get(Issue.PROP_CONFIDENTIAL), false)));
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

		for (Project project: projectManager.query()) {
			undefinedStates.addAll(project.getIssueSetting().getUndefinedStates());
			undefinedStates.addAll(project.getBuildSetting().getUndefinedStates());
		}
		
		for (LinkSpec link: linkSpecManager.query()) { 
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				undefinedStates.addAll(updater.getUndefinedStates());
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationManager.query()) 
			populateUndefinedStates(undefinedStates, setting.getQueries());
		
		for (User user: userManager.query()) 
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
		undefinedFields.addAll(settingManager.getUndefinedIssueFields());
		undefinedFields.addAll(roleManager.getUndefinedIssueFields());
		
		Query<String> query = getSession().createQuery("select distinct name from IssueField");
		for (String fieldName: query.getResultList()) {
			FieldSpec field = getIssueSetting().getFieldSpec(fieldName);
			if (field == null)
				undefinedFields.add(fieldName);
		}

		for (Project project: projectManager.query()) { 
			undefinedFields.addAll(project.getIssueSetting().getUndefinedFields());
			undefinedFields.addAll(project.getBuildSetting().getUndefinedFields());
		}
		
		for (LinkSpec link: linkSpecManager.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				undefinedFields.addAll(updater.getUndefinedFields());
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationManager.query()) 
			populateUndefinedFields(undefinedFields, setting.getQueries());
		
		for (User user: userManager.query()) 
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
			undefinedFieldValues.addAll(project.getIssueSetting().getUndefinedFieldValues());
			undefinedFieldValues.addAll(project.getBuildSetting().getUndefinedFieldValues());
		}
		
		for (LinkSpec link: linkSpecManager.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				undefinedFieldValues.addAll(updater.getUndefinedFieldValues());
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationManager.query()) 
			populateUndefinedFieldValues(undefinedFieldValues, setting.getQueries());
		
		for (User user: userManager.query()) 
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
			project.getIssueSetting().fixUndefinedStates(resolutions);
			project.getBuildSetting().fixUndefinedStates(resolutions);
		}
		
		for (LinkSpec link: linkSpecManager.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				updater.fixUndefinedStates(resolutions);
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationManager.query()) 
			fixUndefinedStates(resolutions, setting.getQueries());

		for (User user: userManager.query())
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
			project.getIssueSetting().fixUndefinedFields(resolutions);
			project.getBuildSetting().fixUndefinedFields(resolutions);
		}
		
		for (LinkSpec link: linkSpecManager.query()) {
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				updater.fixUndefinedFields(resolutions);
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationManager.query())
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
		
		for (Project project: projectManager.query()) {
			project.getIssueSetting().fixUndefinedFieldValues(resolutions);
			project.getBuildSetting().fixUndefinedFieldValues(resolutions);
		}
		
		for (LinkSpec link: linkSpecManager.query()) { 
			for (IssueQueryUpdater updater: link.getQueryUpdaters())
				updater.fixUndefinedFieldValues(resolutions);
		}
		
		for (IssueQueryPersonalization setting: queryPersonalizationManager.query()) 
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

	@Sessional
	@Override
	public List<Issue> query(EntityQuery<Issue> scope, Project project, String term, int count) {
		if (term.contains("#")) {
			String projectPath = StringUtils.substringBefore(term, "#");
			Project specifiedProject = projectManager.findByPath(projectPath);
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
			predicates.addAll(Arrays.asList(getPredicates(null, scope.getCriteria(), criteriaQuery, builder, root)));		
		
		List<Predicate> projectPredicates = new ArrayList<>();
		if (SecurityUtils.canAccessConfidentialIssues(project)) {
			projectPredicates.add(builder.equal(root.get(Issue.PROP_PROJECT), project));
		} else { 
			projectPredicates.add(getNonConfidentialPredicate(builder, root, project));
			projectPredicates.add(getAuthorizationPredicate(criteriaQuery, builder, root, project));
		}
		
		for (Project forkParent: project.getForkParents()) {
			if (SecurityUtils.canAccessConfidentialIssues(forkParent)) {
				projectPredicates.add(builder.equal(root.get(Issue.PROP_PROJECT), forkParent));
			} else if (SecurityUtils.canAccess(forkParent)) {
				projectPredicates.add(getNonConfidentialPredicate(builder, root, forkParent));
				projectPredicates.add(getAuthorizationPredicate(criteriaQuery, builder, root, forkParent));
			}
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
		
		if (scope != null && !scope.getSorts().isEmpty()) {
			criteriaQuery.orderBy(getOrders(scope.getSorts(), builder, root));
		} else {
			criteriaQuery.orderBy(
					builder.desc(root.get(Issue.PROP_PROJECT)), 
					builder.desc(root.get(Issue.PROP_NUMBER)));
		}
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);
		return query.getResultList();
	}
	
	@Transactional
	@Override
	public void delete(Issue issue) {
		super.delete(issue);
	
		Long projectId = issue.getProject().getId();
		Long issueNumber = issue.getNumber();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				issueIds.remove(new ProjectScopedNumber(projectId, issueNumber));
			}
		});
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	if (project.getForkRoot().equals(project))
	    		numberGenerator.removeNextSequence(project);
			
			Long projectId = project.getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					for (var key: issueIds.entrySet().stream()
							.filter(it->it.getKey().getProjectId().equals(projectId))
							.map(it->it.getKey())
							.collect(Collectors.toSet())) {
						issueIds.remove(key);
					}
				}
			});
		}
	}

	private Long getIssueId(Long projectId, Long issueNumber) {
		return issueIds.get(new ProjectScopedNumber(projectId, issueNumber));
	}
	
	@Sessional
	@Override
	public Collection<MilestoneAndIssueState> queryMilestoneAndIssueStates(Project project, Collection<Milestone> milestones) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<MilestoneAndIssueState> criteriaQuery = builder.createQuery(MilestoneAndIssueState.class);
		Root<IssueSchedule> root = criteriaQuery.from(IssueSchedule.class);
		Join<Issue, Issue> issueJoin = root.join(IssueSchedule.PROP_ISSUE, JoinType.INNER);
		criteriaQuery.multiselect(
				root.get(IssueSchedule.PROP_MILESTONE).get(Milestone.PROP_ID), 
				issueJoin.get(Issue.PROP_STATE));
		
		List<Predicate> milestonePredicates = new ArrayList<>();
		for (Milestone milestone: milestones) 
			milestonePredicates.add(builder.equal(root.get(IssueSchedule.PROP_MILESTONE), milestone));
		
		criteriaQuery.where(builder.and(
				getSubtreePredicate(builder, issueJoin.get(Issue.PROP_PROJECT), project),
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
		
		Path<Project> projectPath = root
				.join(IssueSchedule.PROP_ISSUE, JoinType.INNER)
				.get(Issue.PROP_PROJECT);
		
		criteriaQuery.where(getSubtreePredicate(builder, projectPath, project));
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}
	
	private Predicate getSubtreePredicate(CriteriaBuilder builder, Path<Project> projectPath, Project project) {
		Collection<Long> subtreeIds = projectManager.getSubtreeIds(project.getId());
		Collection<Long> allIds = projectManager.getIds();
		return Criteria.forManyValues(builder, projectPath.get(Project.PROP_ID), subtreeIds, allIds);
	}
	
	@Transactional
	@Override
	public void move(Project targetProject, Collection<Issue> issues) {
		List<Pair<Long, String>> attachmentGroupInfos = new ArrayList<>();
		Map<Long, Long> numberMapping = new HashMap<>();
		List<Issue> issueList = new ArrayList<>(issues);
		Collections.sort(issueList);
		for (Issue issue: issueList) {
			attachmentGroupInfos.add(new Pair<>(issue.getAttachmentProject().getId(), issue.getAttachmentGroup()));
			
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
 
			Project oldProject = issue.getProject();
			Project numberScope = targetProject.getForkRoot();
			Long nextNumber = getNextNumber(numberScope);
			issue.setOldVersion(issue.getFacade());
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

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueProjectChangeData(oldProject.getPath(), targetProject.getPath()));
			changeManager.save(change);
		}
		
		for (Issue issue: issueList) {
			if (issue.getDescription() != null) {
				issue.setDescription(new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(issue.getDescription(), "#"));
			}
			
			for (IssueComment comment: issue.getComments()) {
				comment.setContent(new ReferenceMigrator(Issue.class, numberMapping)
						.migratePrefixed(comment.getContent(), "#"));
				commentManager.save(comment);
			}
			save(issue);
		}
		
		Long targetProjectId = targetProject.getId();
		transactionManager.runAfterCommit(new ClusterRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public void run() {
				projectManager.runOnProjectServer(targetProjectId, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() throws Exception {
						for (var attachmentGroupInfo: attachmentGroupInfos) 
							attachmentManager.moveAttachmentGroupTargetLocal(targetProjectId, attachmentGroupInfo.getFirst(), attachmentGroupInfo.getSecond());
						return null;
					}
					
				});
			}
			
		});
	}
	
	@Transactional
	@Override
	public void saveDescription(Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equal(description, prevDescription)) {
			if (description != null && description.length() > Issue.MAX_DESCRIPTION_LEN)
				throw new ExplicitException("Description too long"); 
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
		
		Path<Project> projectPath = root
				.join(IssueSchedule.PROP_ISSUE, JoinType.INNER)
				.get(Issue.PROP_PROJECT);
		criteriaQuery.where(builder.and(
				getSubtreePredicate(builder, projectPath, project),
				builder.or(milestonePredicates.toArray(new Predicate[0]))));
		
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
	public List<ProjectIssueStats> queryStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectIssueStats> criteriaQuery = builder.createQuery(ProjectIssueStats.class);
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
				if (SecurityUtils.canAccessConfidentialIssues(project))
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
			predicates.add(getAuthorizationPredicate(criteriaQuery, builder, root, projectIds));
			
			criteriaQuery.where(builder.or(predicates.toArray(new Predicate[0])));
			criteriaQuery.orderBy(builder.asc(root.get(Issue.PROP_STATE_ORDINAL)));
			
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	@Override
	public Collection<Long> parseFixedIssueIds(Project project, String commitMessage) {
		Collection<Long> issueIds = new HashSet<>();

		// Skip unmatched commit message quickly 
		boolean fixWordsFound = false;
		String lowerCaseCommitMessage = commitMessage.toLowerCase();
		for (String word: ISSUE_FIX_WORDS) {
			if (lowerCaseCommitMessage.indexOf(word) != -1) {
				fixWordsFound = true;
				break;
			}
		}
		
		if (fixWordsFound 
				&& lowerCaseCommitMessage.contains("#") 
				&& lowerCaseCommitMessage.contains("issue")) {
			Matcher matcher = ISSUE_FIX_PATTERN.matcher(lowerCaseCommitMessage);
		
			while (matcher.find()) {
				String projectPath = matcher.group(3);
				Project projectOfIssue;
				if (projectPath == null) 
					projectOfIssue = project;
				else 
					projectOfIssue = projectManager.findByPath(projectPath);
				if (projectOfIssue != null 
						&& (projectOfIssue.isSelfOrAncestorOf(project) || project.isSelfOrAncestorOf(projectOfIssue))) {
					Long issueNumber = Long.parseLong(matcher.group(matcher.groupCount()));
					Long issueId = getIssueId(projectOfIssue.getId(), issueNumber);
					if (issueId != null)
						issueIds.add(issueId);
				}
			}
		}
		
		return issueIds;
	}

	@Override
	public Long getNextNumber(Project numberScope) {
		return numberGenerator.getNextSequence(numberScope);
	}

	@Override
	public void resetNextNumber(Project numberScope) {
		numberGenerator.removeNextSequence(numberScope);
	}

}
