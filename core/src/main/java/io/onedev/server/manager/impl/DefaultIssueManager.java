package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
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
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.issue.NamedQuery;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
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
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.issues.workflowreconcile.InvalidFieldResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValueResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private final IssueFieldUnaryManager issueFieldManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueQuerySettingManager issueQuerySettingManager;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldUnaryManager issueFieldManager, 
			IssueQuerySettingManager issueQuerySettingManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
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
	public void open(Issue issue, Serializable fieldBean) {
		LastActivity lastActivity = new LastActivity();
		lastActivity.setAction("submitted");
		lastActivity.setUser(issue.getSubmitter());
		lastActivity.setDate(issue.getSubmitDate());
		issue.setLastActivity(lastActivity);
		issue.setNumber(getNextNumber(issue.getProject()));
		save(issue);

		StateSpec initialState = issue.getProject().getIssueWorkflow().getInitialStateSpec();
		issueFieldManager.writeFields(issue, fieldBean, initialState.getFields());
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
	
	@Sessional
	@Override
	public List<Issue> query(IssueQuery issueQuery, int firstResult, int maxResults) {
		CriteriaQuery<Issue> criteriaQuery = issueQuery.buildCriteriaQuery(getSession());
		
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (!issues.isEmpty())
			issueFieldManager.populateFields(issues);
		
		return issues;
	}
	
	@Sessional
	@Override
	public int count(IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		if (issueCriteria != null)
			criteriaQuery.where(issueCriteria.getPredicate(new QueryBuildContext(root, builder)));
		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
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
	public Map<String, String> getInvalidFields(Project project) {
		Query query = getSession().createQuery("select distinct name, type from IssueFieldUnary where issue.project=:project");
		query.setParameter("project", project);
		Map<String, String> invalidFields = new HashMap<>();
		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String name = (String) row[0];
			String type = (String) row[1];
			InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(name);
			if (fieldSpec == null || !EditableUtils.getDisplayName(fieldSpec.getClass()).equals(type))
				invalidFields.put(name, type);
		}
		for (String fieldName: project.getIssueListFields()) {
			InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
			if (fieldSpec == null && !invalidFields.containsKey(fieldName)) 
				invalidFields.put(fieldName, null);
		}
		for (NamedQuery namedQuery: project.getSavedIssueQueries()) {
			try {
				for (String undefinedField: IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFields(project)) {
					if (!invalidFields.containsKey(undefinedField))
						invalidFields.put(undefinedField, null);
				}
			} catch (Exception e) {
				
			}
		}
		for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
			for (NamedQuery namedQuery: setting.getUserQueries()) {
				try {
					for (String undefinedField: IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFields(project)) {
						if (!invalidFields.containsKey(undefinedField))
							invalidFields.put(undefinedField, null);
					}
				} catch (Exception e) {
				}
			}
		}
		return invalidFields;
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@Override
	public void fixInvalidFields(Project project, Map<String, InvalidFieldResolution> resolutions) {
		for (Map.Entry<String, InvalidFieldResolution> entry: resolutions.entrySet()) {
			Query query;
			if (entry.getValue().getFixType() == InvalidFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
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
				for (Map.Entry<String, InvalidFieldResolution> resolutionEntry: resolutions.entrySet()) {
					InvalidFieldResolution resolution = resolutionEntry.getValue();
					if (resolution.getFixType() == InvalidFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
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
					for (Map.Entry<String, InvalidFieldResolution> resolutionEntry: resolutions.entrySet()) {
						InvalidFieldResolution resolution = resolutionEntry.getValue();
						if (resolution.getFixType() == InvalidFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
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
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Map<String, String> getUndefinedFieldValues(Project project) {
		Query query = getSession().createQuery("select distinct name, value from IssueFieldUnary where issue.project=:project and type=:choice");
		query.setParameter("project", project);
		query.setParameter("choice", InputSpec.CHOICE);
		Map<String, String> undefinedFieldValues = new HashMap<>();
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
						undefinedFieldValues.put(name, value);
				}
			}
			
			for (NamedQuery namedQuery: project.getSavedIssueQueries()) {
				try {
					undefinedFieldValues.putAll(IssueQuery.parse(project, namedQuery.getQuery(), true).getUndefinedFieldValues(project));
				} catch (Exception e) {
				}
			}
			for (IssueQuerySetting setting: project.getIssueQuerySettings()) {
				for (NamedQuery namedQuery: setting.getUserQueries()) {
					try {
						undefinedFieldValues.putAll(IssueQuery.parse(project, namedQuery.getQuery(), true).getUndefinedFieldValues(project));
					} catch (Exception e) {
					}
				}
			}
			
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
	
}
