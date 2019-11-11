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
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueCommitted;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.AndCriteria;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.facade.IssueFacade;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueManager.class);
	
	private final IssueFieldManager issueFieldManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueQuerySettingManager issueQuerySettingManager;
	
	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final TransactionManager transactionManager;
	
	private final Map<Long, IssueFacade> issues = new HashMap<>();
	
	private final ReadWriteLock issuesLock = new ReentrantReadWriteLock();
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager issueFieldManager, 
			TransactionManager transactionManager, IssueQuerySettingManager issueQuerySettingManager, 
			SettingManager settingManager, ListenerRegistry listenerRegistry, 
			ProjectManager projectManager, UserManager userManager) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
		this.issueQuerySettingManager = issueQuerySettingManager;
		this.listenerRegistry = listenerRegistry;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
		this.userManager = userManager;
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
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("number", number));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Transactional
	@Override
	public void open(Issue issue) {
		Preconditions.checkArgument(issue.isNew());
		Query<?> query = getSession().createQuery("select max(number) from Issue where project=:project");
		query.setParameter("project", issue.getProject());
		issue.setNumber(getNextNumber(issue.getProject(), query));
		save(issue);

		issueFieldManager.saveFields(issue);
		
		listenerRegistry.post(new IssueOpened(issue));
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

	private Predicate[] getPredicates(io.onedev.server.search.entity.EntityCriteria<Issue> criteria, 
			@Nullable Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		List<Predicate> predicates = new ArrayList<>();
		if (project != null) {
			predicates.add(builder.equal(root.get("project"), project));
		} else if (!SecurityUtils.isAdministrator()) {
			List<Predicate> projectPredicates = new ArrayList<>();
			for (Project each: projectManager.getAccessibleProjects(user)) 
				projectPredicates.add(builder.equal(root.get(IssueConstants.ATTR_PROJECT), each));
			predicates.add(builder.or(projectPredicates.toArray(new Predicate[projectPredicates.size()])));
		}
		if (criteria != null)
			predicates.add(criteria.getPredicate(project, root, builder, user));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Issue> buildCriteriaQuery(Session session, Project project, EntityQuery<Issue> issueQuery, User user) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);
		
		query.where(getPredicates(issueQuery.getCriteria(), project, root, builder, user));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: issueQuery.getSorts()) {
			if (IssueConstants.ORDER_FIELDS.containsKey(sort.getField())) {
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(IssueQuery.getPath(root, IssueConstants.ORDER_FIELDS.get(sort.getField()))));
				else
					orders.add(builder.desc(IssueQuery.getPath(root, IssueConstants.ORDER_FIELDS.get(sort.getField()))));
			} else {
				Join<Issue, IssueField> join = root.join(IssueConstants.ATTR_FIELDS, JoinType.LEFT);
				join.on(builder.equal(join.get(IssueField.ATTR_NAME), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get(IssueField.ATTR_ORDINAL)));
				else
					orders.add(builder.desc(join.get(IssueField.ATTR_ORDINAL)));
			}
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get("number")));
		query.orderBy(orders);
		
		return query;
	}
	
	private IssueSetting getIssueSetting() {
		return settingManager.getIssueSetting();
	}

	@Sessional
	@Override
	public List<Issue> query(@Nullable Project project, User user, io.onedev.server.search.entity.EntityQuery<Issue> issueQuery, 
			int firstResult, int maxResults) {
		CriteriaQuery<Issue> criteriaQuery = buildCriteriaQuery(getSession(), project, issueQuery, user);
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (!issues.isEmpty())
			issueFieldManager.populateFields(issues);
		
		return issues;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		if (!(event instanceof IssueCommitted))
			event.getIssue().setUpdateDate(event.getDate());
	}
	
	@Sessional
	@Override
	public int count(@Nullable Project project, User user, IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		criteriaQuery.where(getPredicates(issueCriteria, project, root, builder, user));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public int count(Milestone milestone, User user, @Nullable StateSpec.Category category) {
		if (category != null) {
			IssueCriteria criteria = getIssueSetting().getCategoryCriteria(category);
			if (criteria != null) {
				List<IssueCriteria> criterias = new ArrayList<>();
				criterias.add(new MilestoneCriteria(milestone.getName()));
				criterias.add(criteria);
				return count(milestone.getProject(), user, new AndCriteria(criterias));
			} else {
				return 0;
			}
		} else {
			IssueCriteria criteria = new MilestoneCriteria(milestone.getName());
			return count(milestone.getProject(), user, criteria);
		}
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Collection<String> getUndefinedStates() {
		Query<String> query = getSession().createQuery("select distinct state from Issue");
		
		Collection<String> undefinedStates = new HashSet<>(query.getResultList());
		for (Iterator<String> it = undefinedStates.iterator(); it.hasNext();) {
			if (getIssueSetting().getStateSpec(it.next()) != null)
				it.remove();
		}

		for (Project project: projectManager.query())
			undefinedStates.addAll(project.getIssueSetting().getUndefinedStates(project));
		
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
				undefinedStates.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedStates());
			} catch (Exception e) {
			}
		}
	}

	@Transactional
	@Override
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			Query<?> query = getSession().createQuery("update Issue set state=:newState where state=:oldState");
			query.setParameter("oldState", entry.getKey());
			query.setParameter("newState", entry.getValue().getNewState());
			query.executeUpdate();
		}
		
		for (Project project: projectManager.query()) 
			project.getIssueSetting().fixUndefinedStates(resolutions);
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			fixUndefinedStates(resolutions, setting.getProject(), setting.getUserQueries());

		for (User user: userManager.query())
			fixUndefinedStates(resolutions, null, user.getIssueQuerySetting().getUserQueries());
	}
	
	private void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (NamedIssueQuery namedQuery: namedQueries) {
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
				for (Map.Entry<String, UndefinedStateResolution> resolutionEntry: resolutions.entrySet())
					query.onRenameState(resolutionEntry.getKey(), resolutionEntry.getValue().getNewState());
				namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
	}

	@Sessional
	@Override
	public List<Issue> query(Project project, String term, int count) {
		List<Issue> issues = new ArrayList<>();

		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		
		if (StringUtils.isNotBlank(term)) {
			if (term.startsWith("#")) {
				term = term.substring(1);
				try {
					long buildNumber = Long.parseLong(term);
					criteria.add(Restrictions.eq("number", buildNumber));
				} catch (NumberFormatException e) {
					criteria.add(Restrictions.or(
							Restrictions.ilike("title", "#" + term, MatchMode.ANYWHERE),
							Restrictions.ilike("noSpaceTitle", "#" + term, MatchMode.ANYWHERE)));
				}
			} else try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("title", term, MatchMode.ANYWHERE),
						Restrictions.ilike("noSpaceTitle", term, MatchMode.ANYWHERE)));
			}
		}
		
		criteria.addOrder(Order.desc("number"));
		issues.addAll(query(criteria, 0, count));
		
		return issues;
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Collection<String> getUndefinedFields() {
		Query<?> query = getSession().createQuery("select distinct name from IssueField");
		Set<String> undefinedFields = new HashSet<>();
		for (String fieldName: (List<String>)query.getResultList()) {
			FieldSpec field = getIssueSetting().getFieldSpec(fieldName);
			if (field == null)
				undefinedFields.add(fieldName);
		}

		for (Project project: projectManager.query()) 
			undefinedFields.addAll(project.getIssueSetting().getUndefinedFields(project));
		
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
				undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFields());
			} catch (Exception e) {
			}
		}
	}

	@Transactional
	@Override
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
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
		
		for (Project project: projectManager.query()) 
			project.getIssueSetting().fixUndefinedFields(resolutions);
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query())
			fixUndefinedFields(resolutions, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query()) 
			fixUndefinedFields(resolutions, null, user.getIssueQuerySetting().getUserQueries());
	}
	
	private void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
				boolean remove = false;
				for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
					UndefinedFieldResolution resolution = entry.getValue();
					if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
						query.onRenameField(entry.getKey(), resolution.getNewField());
					} else if (query.onDeleteField(entry.getKey())) {
						remove = true;
						break;
					}
				}				
				if (remove)
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Query query = getSession().createQuery("select distinct name, value from IssueField where type=:choice");
		query.setParameter("choice", FieldSpec.ENUMERATION);
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String fieldName = (String) row[0];
			String fieldValue = (String) row[1];
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(fieldName));
			if (specifiedChoices != null && !specifiedChoices.getChoiceValues().contains(fieldValue)) {
				undefinedFieldValues.add(new UndefinedFieldValue(fieldName, fieldValue));
			}
		}

		for (Project project: projectManager.query())
			undefinedFieldValues.addAll(project.getIssueSetting().getUndefinedFieldValues());
		
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
				undefinedFieldValues.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@Override
	public void fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits) {
		for (Map.Entry<String, ValueSetEdit> entry: valueSetEdits.entrySet()) {
			for (String deletion: entry.getValue().getDeletions()) {
				Query query = getSession().createQuery("delete from IssueField where name=:fieldName and value=:fieldValue");
				query.setParameter("fieldName", entry.getKey());
				query.setParameter("fieldValue", deletion);
				query.executeUpdate();
			}
			for (Map.Entry<String, String> renameEntry: entry.getValue().getRenames().entrySet()) {
				Query query = getSession().createQuery("update IssueField set value=:newValue where name=:fieldName and value=:oldValue");
				query.setParameter("fieldName", entry.getKey());
				query.setParameter("oldValue", renameEntry.getKey());
				query.setParameter("newValue", renameEntry.getValue());
				query.executeUpdate();
			}
		}
		
		for (Project project: projectManager.query()) {
			project.getIssueSetting().fixUndefinedFieldValues(valueSetEdits);
		}
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) 
			fixUndefinedFieldValues(valueSetEdits, setting.getProject(), setting.getUserQueries());
		
		for (User user: userManager.query())
			fixUndefinedFieldValues(valueSetEdits, null, user.getIssueQuerySetting().getUserQueries());
	}
	
	private void fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits, 
			@Nullable Project project, List<NamedIssueQuery> namedQueries) {
		for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
				if (query.fixUndefinedFieldValues(valueSetEdits))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
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
}
