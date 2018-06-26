package io.onedev.server.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueQuerySettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.model.support.issue.NamedQuery;
import io.onedev.server.model.support.issue.query.AndCriteria;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueQueryLexer;
import io.onedev.server.model.support.issue.query.IssueSort;
import io.onedev.server.model.support.issue.query.IssueSort.Direction;
import io.onedev.server.model.support.issue.query.MilestoneCriteria;
import io.onedev.server.model.support.issue.query.QueryBuildContext;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValueResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private final IssueFieldUnaryManager issueFieldUnaryManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueQuerySettingManager issueQuerySettingManager;

	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldUnaryManager issueFieldUnaryManager, 
			IssueQuerySettingManager issueQuerySettingManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueFieldUnaryManager = issueFieldUnaryManager;
		this.issueQuerySettingManager = issueQuerySettingManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Sessional
	@Override
	public Issue find(Project target, long number) {
		EntityCriteria<Issue> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", target));
		criteria.add(Restrictions.eq("number", number));
		return find(criteria);
	}
	
	@Transactional
	@Override
	public void open(Issue issue) {
		LastActivity lastActivity = new LastActivity();
		lastActivity.setAction("submitted");
		lastActivity.setUser(issue.getSubmitter());
		lastActivity.setDate(issue.getSubmitDate());
		issue.setLastActivity(lastActivity);
		issue.setNumber(getNextNumber(issue.getProject()));
		save(issue);

		issueFieldUnaryManager.saveFields(issue);
		
		listenerRegistry.post(new IssueOpened(issue));
	}

	private long getNextNumber(Project project) {
		AtomicLong nextNumber;
		synchronized (nextNumbers) {
			nextNumber = nextNumbers.get(project.getUUID());
		}
		if (nextNumber == null) {
			long maxNumber;
			Query<?> query = getSession().createQuery("select max(number) from Issue where project=:project");
			query.setParameter("project", project);
			Object result = query.uniqueResult();
			if (result != null) {
				maxNumber = (Long)result;
			} else {
				maxNumber = 0;
			}
			
			/*
			 * do not put the whole method in synchronized block to avoid possible deadlocks
			 * if there are limited connections. 
			 */
			synchronized (nextNumbers) {
				nextNumber = nextNumbers.get(project.getUUID());
				if (nextNumber == null) {
					nextNumber = new AtomicLong(maxNumber+1);
					nextNumbers.put(project.getUUID(), nextNumber);
				}
			}
		} 
		return nextNumber.getAndIncrement();
	}
	
	private Predicate[] getPredicates(IssueCriteria criteria, Project project, QueryBuildContext context) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(context.getBuilder().equal(context.getRoot().get("project"), project));
		if (criteria != null)
			predicates.add(criteria.getPredicate(project, context));
		return predicates.toArray(new Predicate[0]);
	}
	
	public CriteriaQuery<Issue> buildCriteriaQuery(Session session, Project project, IssueQuery issueQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);
		
		QueryBuildContext context = new QueryBuildContext(root, builder);
		query.where(getPredicates(issueQuery.getCriteria(), project, context));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (IssueSort sort: issueQuery.getSorts()) {
			if (Issue.BUILTIN_FIELDS.containsKey(sort.getField())) {
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(IssueQuery.getPath(root, Issue.BUILTIN_FIELDS.get(sort.getField()))));
				else
					orders.add(builder.desc(IssueQuery.getPath(root, Issue.BUILTIN_FIELDS.get(sort.getField()))));
			} else {
				Join<Issue, IssueFieldUnary> join = root.join("fieldUnaries", JoinType.LEFT);
				join.on(builder.equal(join.get(IssueFieldUnary.NAME), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get(IssueFieldUnary.ORDINAL)));
				else
					orders.add(builder.desc(join.get(IssueFieldUnary.ORDINAL)));
			}
		}

		Path<String> idPath = root.get("id");
		if (orders.isEmpty())
			orders.add(builder.desc(idPath));
		query.orderBy(orders);
		
		return query;
	}

	@Sessional
	@Override
	public List<Issue> query(Project project, IssueQuery issueQuery, int firstResult, int maxResults) {
		CriteriaQuery<Issue> criteriaQuery = buildCriteriaQuery(getSession(), project, issueQuery);
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (!issues.isEmpty())
			issueFieldUnaryManager.populateFields(issues);
		
		return issues;
	}
	
	@Sessional
	@Override
	public int count(Project project, IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		QueryBuildContext context = new QueryBuildContext(root, builder);
		criteriaQuery.where(getPredicates(issueCriteria, project, context));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public int count(Milestone milestone, @Nullable StateSpec.Category category) {
		if (category != null) {
			IssueCriteria criteria = milestone.getProject().getIssueWorkflow().getCategoryCriteria(category);
			if (criteria != null) {
				List<IssueCriteria> criterias = new ArrayList<>();
				criterias.add(new MilestoneCriteria(milestone.getName(), IssueQueryLexer.Is));
				criterias.add(criteria);
				return count(milestone.getProject(), new AndCriteria(criterias));
			} else {
				return 0;
			}
		} else {
			IssueCriteria criteria = new MilestoneCriteria(milestone.getName(), IssueQueryLexer.Is);
			return count(milestone.getProject(), criteria);
		}
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Collection<String> getUndefinedStates(Project project) {
		Query<String> query = getSession().createQuery("select distinct state from Issue where project=:project");
		query.setParameter("project", project);
		
		Set<String> states = new HashSet<>(query.getResultList());
		for (Iterator<String> it = states.iterator(); it.hasNext();) {
			if (project.getIssueWorkflow().getStateSpec(it.next()) != null)
				it.remove();
		}

		for (NamedQuery namedQuery: project.getSavedIssueQueries()) {
			try {
				states.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedStates(project));
			} catch (Exception e) {
			}
		}
		
		for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
			for (NamedQuery namedQuery: setting.getUserQueries()) {
				try {
					states.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedStates(project));
				} catch (Exception e) {
				}
			}
		}
		
		for (IssueBoard board: project.getIssueBoards())
			states.addAll(board.getUndefinedStates(project));

		return states;
	}

	@Transactional
	@Override
	public void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions) {
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			Query<?> query = getSession().createQuery("update Issue set state=:newState where state=:oldState and project=:project");
			query.setParameter("project", project);
			query.setParameter("oldState", entry.getKey());
			query.setParameter("newState", entry.getValue().getNewState());
			query.executeUpdate();
		}
		
		for (NamedQuery namedQuery: project.getSavedIssueQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
				for (Map.Entry<String, UndefinedStateResolution> resolutionEntry: resolutions.entrySet())
					query.onRenameState(resolutionEntry.getKey(), resolutionEntry.getValue().getNewState());
				namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
			for (NamedQuery namedQuery: setting.getUserQueries()) {
				try {
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
					for (Map.Entry<String, UndefinedStateResolution> resolutionEntry: resolutions.entrySet())
						query.onRenameState(resolutionEntry.getKey(), resolutionEntry.getValue().getNewState());
					namedQuery.setQuery(query.toString());
				} catch (Exception e) {
				}
			}
			issueQuerySettingManager.save(setting);
		}
		
		for (IssueBoard board: project.getIssueBoards())
			board.fixUndefinedStates(project, resolutions);
	}

	@Sessional
	@Override
	public List<Issue> query(Project project, String term, int count) {
		List<Issue> issues = new ArrayList<>();
		
		Long number = null;
		String numberStr = term;
		if (numberStr != null) {
			numberStr = numberStr.trim();
			if (numberStr.startsWith("#"))
				numberStr = numberStr.substring(1);
			if (StringUtils.isNumeric(numberStr))
				number = Long.valueOf(numberStr);
		}
		
		if (number != null) {
			Issue issue = OneDev.getInstance(IssueManager.class).find(project, number);
			if (issue != null)
				issues.add(issue);
			EntityCriteria<Issue> criteria = EntityCriteria.of(Issue.class);
			criteria.add(Restrictions.eq("project", project));
			criteria.add(Restrictions.and(
					Restrictions.or(Restrictions.ilike("noSpaceTitle", "%" + term + "%"), Restrictions.ilike("numberStr", term + "%")), 
					Restrictions.ne("number", number)
				));
			criteria.addOrder(Order.desc("number"));
			issues.addAll(OneDev.getInstance(IssueManager.class).findRange(criteria, 0, count-issues.size()));
		} else {
			EntityCriteria<Issue> criteria = EntityCriteria.of(Issue.class);
			criteria.add(Restrictions.eq("project", project));
			if (StringUtils.isNotBlank(term)) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("noSpaceTitle", "%" + term + "%"), 
						Restrictions.ilike("numberStr", (term.startsWith("#")? term.substring(1): term) + "%")));
			}
			criteria.addOrder(Order.desc("number"));
			issues.addAll(OneDev.getInstance(IssueManager.class).findRange(criteria, 0, count));
		} 
		return issues;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Collection<String> getUndefinedFields(Project project) {
		Query query = getSession().createQuery("select distinct name from IssueFieldUnary where issue.project=:project");
		query.setParameter("project", project);
		Set<String> undefinedFields = new HashSet<>();
		for (String name: (List<String>)query.getResultList()) {
			if (project.getIssueWorkflow().getFieldSpec(name) == null)
				undefinedFields.add(name);
		}
		for (String fieldName: project.getIssueListFields()) {
			if (!Issue.BUILTIN_FIELDS.containsKey(fieldName)) {
				if (project.getIssueWorkflow().getFieldSpec(fieldName) == null)
					undefinedFields.add(fieldName);
			}
		}
		for (NamedQuery namedQuery: project.getSavedIssueQueries()) {
			try {
				undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFields(project));
			} catch (Exception e) {
			}
		}
		for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
			for (NamedQuery namedQuery: setting.getUserQueries()) {
				try {
					undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFields(project));
				} catch (Exception e) {
				}
			}
		}
		for (IssueBoard board: project.getIssueBoards()) 
			undefinedFields.addAll(board.getUndefinedFields(project));
		return undefinedFields;
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@Override
	public void fixUndefinedFields(Project project, Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			Query query;
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				query = getSession().createQuery("update IssueFieldUnary set name=:newName where name=:oldName and issue.id in (select id from Issue where project=:project)");
				query.setParameter("oldName", entry.getKey());
				query.setParameter("newName", entry.getValue().getNewField());
				
				int index = project.getIssueListFields().indexOf(entry.getKey());
				if (index != -1)
					project.getIssueListFields().set(index, entry.getValue().getNewField());
			} else {
				query = getSession().createQuery("delete from IssueFieldUnary where name=:fieldName and issue.id in (select id from Issue where project=:project)");
				query.setParameter("fieldName", entry.getKey());
				project.getIssueListFields().remove(entry.getKey());
			}
			query.setParameter("project", project);
			query.executeUpdate();
		}
		
		for (Iterator<NamedQuery> it = project.getSavedIssueQueries().iterator(); it.hasNext();) {
			NamedQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
				boolean remove = false;
				for (Map.Entry<String, UndefinedFieldResolution> resolutionEntry: resolutions.entrySet()) {
					UndefinedFieldResolution resolution = resolutionEntry.getValue();
					if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
						query.onRenameField(resolutionEntry.getKey(), resolution.getNewField());
					} else if (query.onDeleteField(resolutionEntry.getKey())) {
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
		for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
			for (Iterator<NamedQuery> it = setting.getUserQueries().iterator(); it.hasNext();) {
				NamedQuery namedQuery = it.next();
				try {
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
					boolean remove = false;
					for (Map.Entry<String, UndefinedFieldResolution> resolutionEntry: resolutions.entrySet()) {
						UndefinedFieldResolution resolution = resolutionEntry.getValue();
						if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
							query.onRenameField(resolutionEntry.getKey(), resolution.getNewField());
						} else if (query.onDeleteField(resolutionEntry.getKey())) {
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
			issueQuerySettingManager.save(setting);
		}
		
		for (Iterator<IssueBoard> it = project.getIssueBoards().iterator(); it.hasNext();) {
			IssueBoard board = it.next();
			if (board.fixUndefinedFields(project, resolutions))
				it.remove();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		Query query = getSession().createQuery("select distinct name, value from IssueFieldUnary where issue.project=:project and type=:choice");
		query.setParameter("project", project);
		query.setParameter("choice", InputSpec.CHOICE);
		Set<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		OneContext.push(new OneContext() {

			@Override
			public Project getProject() {
				return project;
			}

			@Override
			public EditContext getEditContext(int level) {
				return new EditContext() {

					@Override
					public Object getInputValue(String name) {
						return null;
					}
					
				};
			}

			@Override
			public InputContext getInputContext() {
				throw new UnsupportedOperationException();
			}
			
		});
		try {
			for (Object[] row: (List<Object[]>)query.getResultList()) {
				String name = (String) row[0];
				String value = (String) row[1];
				InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(name);
				if (fieldSpec != null && value != null) {
					List<String> choices = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
					if (!choices.contains(value))
						undefinedFieldValues.add(new UndefinedFieldValue(name, value));
				}
			}
			
			for (NamedQuery namedQuery: project.getSavedIssueQueries()) {
				try {
					undefinedFieldValues.addAll(IssueQuery.parse(project, namedQuery.getQuery(), true).getUndefinedFieldValues(project));
				} catch (Exception e) {
				}
			}
			for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
				for (NamedQuery namedQuery: setting.getUserQueries()) {
					try {
						undefinedFieldValues.addAll(IssueQuery.parse(project, namedQuery.getQuery(), true).getUndefinedFieldValues(project));
					} catch (Exception e) {
					}
				}
			}
			for (IssueBoard board: project.getIssueBoards())
				undefinedFieldValues.addAll(board.getUndefinedFieldValues(project));
			
			return undefinedFieldValues;
		} finally {
			OneContext.pop();
		}
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@Override
	public void fixUndefinedFieldValues(Project project, Map<UndefinedFieldValue, UndefinedFieldValueResolution> resolutions) {
		for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> entry: resolutions.entrySet()) {
			Query query;
			if (entry.getValue().getFixType() == UndefinedFieldValueResolution.FixType.CHANGE_TO_ANOTHER_VALUE) {
				query = getSession().createQuery("update IssueFieldUnary set value=:newValue where name=:fieldName and value=:oldValue and issue.id in (select id from Issue where project=:project)");
				query.setParameter("fieldName", entry.getKey().getFieldName());
				query.setParameter("oldValue", entry.getKey().getFieldValue());
				query.setParameter("newValue", entry.getValue().getNewValue());
			} else {
				query = getSession().createQuery("delete from IssueFieldUnary where name=:fieldName and value=:fieldValue and issue.id in (select id from Issue where project=:project)");
				query.setParameter("fieldName", entry.getKey().getFieldName());
				query.setParameter("fieldValue", entry.getKey().getFieldValue());
			}
			query.setParameter("project", project);
			query.executeUpdate();
		}
		
		for (Iterator<NamedQuery> it = project.getSavedIssueQueries().iterator(); it.hasNext();) {
			NamedQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), true);
				boolean remove = false;
				for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> resolutionEntry: resolutions.entrySet()) {
					UndefinedFieldValueResolution resolution = resolutionEntry.getValue();
					if (resolution.getFixType() == UndefinedFieldValueResolution.FixType.CHANGE_TO_ANOTHER_VALUE) {
						query.onRenameFieldValue(resolutionEntry.getKey().getFieldName(), resolutionEntry.getKey().getFieldValue(), 
								resolutionEntry.getValue().getNewValue());
					} else if (query.onDeleteFieldValue(resolutionEntry.getKey().getFieldName(), resolutionEntry.getKey().getFieldValue())) {
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
		for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
			for (Iterator<NamedQuery> it = setting.getUserQueries().iterator(); it.hasNext();) {
				NamedQuery namedQuery = it.next();
				try {
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), true);
					boolean remove = false;
					for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> resolutionEntry: resolutions.entrySet()) {
						UndefinedFieldValueResolution resolution = resolutionEntry.getValue();
						if (resolution.getFixType() == UndefinedFieldValueResolution.FixType.CHANGE_TO_ANOTHER_VALUE) {
							query.onRenameFieldValue(resolutionEntry.getKey().getFieldName(), resolutionEntry.getKey().getFieldValue(), 
									resolutionEntry.getValue().getNewValue());
						} else if (query.onDeleteFieldValue(resolutionEntry.getKey().getFieldName(), resolutionEntry.getKey().getFieldValue())) {
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
			issueQuerySettingManager.save(setting);
		}
		
		for (Iterator<IssueBoard> it = project.getIssueBoards().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedFieldValues(project, resolutions))
				it.remove();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Transactional
	@Override
	public void fixFieldValueOrders(Project project) {
		OneContext.push(new OneContext() {

			@Override
			public Project getProject() {
				return project;
			}

			@Override
			public EditContext getEditContext(int level) {
				return new EditContext() {

					@Override
					public Object getInputValue(String name) {
						return null;
					}
					
				};
			}

			@Override
			public InputContext getInputContext() {
				throw new UnsupportedOperationException();
			}
			
		});
		try {
			Query query = getSession().createQuery("select distinct name, value, ordinal from IssueFieldUnary where issue.project=:project and type=:choice");
			query.setParameter("project", project);
			query.setParameter("choice", InputSpec.CHOICE);

			for (Object[] row: (List<Object[]>)query.getResultList()) {
				String name = (String) row[0];
				String value = (String) row[1];
				long ordinal = (long) row[2];
				InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(name);
				if (fieldSpec != null) {
					List<String> choices = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
					long newOrdinal = choices.indexOf(value);
					if (ordinal != newOrdinal) {
						query = getSession().createQuery("update IssueFieldUnary set ordinal=:newOrdinal where name=:fieldName and value=:fieldValue and issue.id in (select id from Issue where project=:project)");
						query.setParameter("fieldName", name);
						query.setParameter("fieldValue", value);
						query.setParameter("newOrdinal", newOrdinal);
						query.setParameter("project", project);
						query.executeUpdate();
					}
				}
			}
		} finally {
			OneContext.pop();
		}
		
	}

	/*
	@Transactional
	@Override
	public void test() {
		Project project = OneDev.getInstance(ProjectManager.class).load(1L);
		Milestone m1 = OneDev.getInstance(MilestoneManager.class).load(1L);
		Milestone m2 = OneDev.getInstance(MilestoneManager.class).load(2L);
		User user = OneDev.getInstance(UserManager.class).load(1L);
		for (int i=1; i<=100000; i++) {
			Issue issue = new Issue();
			issue.setProject(project);
			if (i<=50000)
				issue.setMilestone(m1);
			else
				issue.setMilestone(m2);
			issue.setTitle("issue " + i);
			issue.setSubmitter(user);
			issue.setSubmitDate(new Date());
			issue.setBoardPosition(1);
			issue.setState("Open");
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("submitted");
			lastActivity.setUser(issue.getSubmitter());
			lastActivity.setDate(issue.getSubmitDate());
			issue.setLastActivity(lastActivity);
			issue.setNumber(getNextNumber(issue.getProject()));
			issue.setLastActivity(lastActivity);
			save(issue);
		}
	}
	*/

}
