package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.IssueFieldEntityManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.issue.IssueCommitted;
import io.onedev.server.event.issue.IssueDeleted;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldEntity;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.search.entity.issue.AndCriteria;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryBuildContext;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private final IssueFieldEntityManager issueFieldEntityManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueQuerySettingManager issueQuerySettingManager;
	
	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;

	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldEntityManager issueFieldEntityManager, 
			IssueQuerySettingManager issueQuerySettingManager, SettingManager settingManager, 
			ListenerRegistry listenerRegistry, ProjectManager projectManager, 
			UserManager userManager) {
		super(dao);
		this.issueFieldEntityManager = issueFieldEntityManager;
		this.issueQuerySettingManager = issueQuerySettingManager;
		this.listenerRegistry = listenerRegistry;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.userManager = userManager;
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

		issueFieldEntityManager.saveFields(issue);
		
		listenerRegistry.post(new IssueOpened(issue));
	}

	private Predicate[] getPredicates(io.onedev.server.search.entity.EntityCriteria<Issue> criteria, Project project, QueryBuildContext<Issue> context, User user) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(context.getBuilder().equal(context.getRoot().get("project"), project));
		if (criteria != null)
			predicates.add(criteria.getPredicate(project, context, user));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Issue> buildCriteriaQuery(Session session, Project project, EntityQuery<Issue> issueQuery, User user) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);
		
		QueryBuildContext<Issue> context = new IssueQueryBuildContext(root, builder);
		query.where(getPredicates(issueQuery.getCriteria(), project, context, user));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: issueQuery.getSorts()) {
			if (IssueConstants.ORDER_FIELDS.containsKey(sort.getField())) {
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(IssueQuery.getPath(root, IssueConstants.ORDER_FIELDS.get(sort.getField()))));
				else
					orders.add(builder.desc(IssueQuery.getPath(root, IssueConstants.ORDER_FIELDS.get(sort.getField()))));
			} else {
				Join<Issue, IssueFieldEntity> join = root.join(IssueConstants.ATTR_FIELD_ENTITIES, JoinType.LEFT);
				join.on(builder.equal(join.get(IssueFieldEntity.FIELD_ATTR_NAME), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get(IssueFieldEntity.FIELD_ATTR_ORDINAL)));
				else
					orders.add(builder.desc(join.get(IssueFieldEntity.FIELD_ATTR_ORDINAL)));
			}
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get("number")));
		query.orderBy(orders);
		
		return query;
	}
	
	private GlobalIssueSetting getGlobalIssueSetting() {
		return settingManager.getIssueSetting();
	}

	@Sessional
	@Override
	public List<Issue> query(Project project, User user, io.onedev.server.search.entity.EntityQuery<Issue> issueQuery, int firstResult, int maxResults) {
		CriteriaQuery<Issue> criteriaQuery = buildCriteriaQuery(getSession(), project, issueQuery, user);
		Query<Issue> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		List<Issue> issues = query.getResultList();
		if (!issues.isEmpty())
			issueFieldEntityManager.populateFields(issues);
		
		return issues;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		if (!(event instanceof IssueDeleted) && !(event instanceof IssueCommitted))
			event.getIssue().setUpdateDate(event.getDate());
	}
	
	@Sessional
	@Override
	public int count(Project project, User user, IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);

		QueryBuildContext<Issue> context = new IssueQueryBuildContext(root, builder);
		criteriaQuery.where(getPredicates(issueCriteria, project, context, user));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public int count(Milestone milestone, User user, @Nullable StateSpec.Category category) {
		if (category != null) {
			IssueCriteria criteria = getGlobalIssueSetting().getCategoryCriteria(category);
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
		
		Set<String> undefinedStates = new HashSet<>(query.getResultList());
		for (Iterator<String> it = undefinedStates.iterator(); it.hasNext();) {
			if (getGlobalIssueSetting().getStateSpec(it.next()) != null)
				it.remove();
		}

		for (Project project: projectManager.query()) {
			undefinedStates.addAll(project.getIssueSetting().getUndefinedStates(project));
		}
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) {
			for (NamedIssueQuery namedQuery: setting.getUserQueries()) {
				try {
					undefinedStates.addAll(IssueQuery.parse(setting.getProject(), namedQuery.getQuery(), false).getUndefinedStates());
				} catch (Exception e) {
				}
			}
		}

		return undefinedStates;
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
		
		for (Project project: projectManager.query()) {
			project.getIssueSetting().fixUndefinedStates(resolutions);
		}
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) {
			for (NamedIssueQuery namedQuery: setting.getUserQueries()) {
				try {
					IssueQuery query = IssueQuery.parse(setting.getProject(), namedQuery.getQuery(), false);
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

		EntityCriteria<Issue> criteria = newCriteria();
		
		if (term == null)
			term = "";
		
		if (term.startsWith("#")) {
			term = term.substring(1);
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("title", "%#" + term + "%"),
						Restrictions.ilike("noSpaceTitle", "%#" + term + "%")));
			}
		} else {
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("title", "%" + term + "%"),
						Restrictions.ilike("noSpaceTitle", "%" + term + "%")));
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
		Query<?> query = getSession().createQuery("select distinct name from IssueFieldEntity");
		Set<String> undefinedFields = new HashSet<>();
		for (String fieldName: (List<String>)query.getResultList()) {
			InputSpec field = getGlobalIssueSetting().getFieldSpec(fieldName);
			if (field == null)
				undefinedFields.add(fieldName);
		}
		for (Project project: projectManager.query()) {
			undefinedFields.addAll(project.getIssueSetting().getUndefinedFields(project));
		}
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) {
			for (NamedIssueQuery namedQuery: setting.getUserQueries()) {
				try {
					undefinedFields.addAll(IssueQuery.parse(setting.getProject(), namedQuery.getQuery(), false).getUndefinedFields());
				} catch (Exception e) {
				}
			}
		}
		return undefinedFields;
	}

	@Transactional
	@Override
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			Query<?> query;
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				query = getSession().createQuery("update IssueFieldEntity set name=:newName where name=:oldName");
				query.setParameter("oldName", entry.getKey());
				query.setParameter("newName", entry.getValue().getNewField());
			} else {
				query = getSession().createQuery("delete from IssueFieldEntity where name=:fieldName");
				query.setParameter("fieldName", entry.getKey());
			}				
			query.executeUpdate();
		}
		
		for (Project project: projectManager.query()) {
			project.getIssueSetting().fixUndefinedFields(resolutions);
		}
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) {
			for (Iterator<NamedIssueQuery> it = setting.getUserQueries().iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
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
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Sessional
	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Query query = getSession().createQuery("select distinct name, value from IssueFieldEntity where type=:choice");
		query.setParameter("choice", InputSpec.ENUMERATION);
		Set<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String fieldName = (String) row[0];
			String fieldValue = (String) row[1];
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getGlobalIssueSetting().getFieldSpec(fieldName));
			if (specifiedChoices != null && !specifiedChoices.getChoiceValues().contains(fieldValue)) {
				undefinedFieldValues.add(new UndefinedFieldValue(fieldName, fieldValue));
			}
		}

		for (Project project: projectManager.query())
			undefinedFieldValues.addAll(project.getIssueSetting().getUndefinedFieldValues());
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) {
			for (NamedIssueQuery namedQuery: setting.getUserQueries()) {
				try {
					undefinedFieldValues.addAll(IssueQuery.parse(setting.getProject(), namedQuery.getQuery(), false).getUndefinedFieldValues());
				} catch (Exception e) {
				}
			}
		}
		
		return undefinedFieldValues;
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@Override
	public void fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits) {
		for (Map.Entry<String, ValueSetEdit> entry: valueSetEdits.entrySet()) {
			for (String deletion: entry.getValue().getDeletions()) {
				Query query = getSession().createQuery("delete from IssueFieldEntity where name=:fieldName and value=:fieldValue");
				query.setParameter("fieldName", entry.getKey());
				query.setParameter("fieldValue", deletion);
				query.executeUpdate();
			}
			for (Map.Entry<String, String> renameEntry: entry.getValue().getRenames().entrySet()) {
				Query query = getSession().createQuery("update IssueFieldEntity set value=:newValue where name=:fieldName and value=:oldValue");
				query.setParameter("fieldName", entry.getKey());
				query.setParameter("oldValue", renameEntry.getKey());
				query.setParameter("newValue", renameEntry.getValue());
				query.executeUpdate();
			}
		}
		
		for (Project project: projectManager.query()) {
			project.getIssueSetting().fixUndefinedFieldValues(valueSetEdits);
		}
		
		for (IssueQuerySetting setting: issueQuerySettingManager.query()) {
			for (Iterator<NamedIssueQuery> it = setting.getUserQueries().iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
					if (query.fixUndefinedFieldValues(valueSetEdits))
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
	public void fixFieldValueOrders() {
		Query query = getSession().createQuery("select distinct name, value, ordinal from IssueFieldEntity where type=:choice");
		query.setParameter("choice", InputSpec.ENUMERATION);

		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String name = (String) row[0];
			String value = (String) row[1];
			long ordinal = (long) row[2];
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getGlobalIssueSetting().getFieldSpec(name));
			if (specifiedChoices != null) {
				long newOrdinal = specifiedChoices.getChoiceValues().indexOf(value);
				if (ordinal != newOrdinal) {
					query = getSession().createQuery("update IssueFieldEntity set ordinal=:newOrdinal where name=:fieldName and value=:fieldValue");
					query.setParameter("fieldName", name);
					query.setParameter("fieldValue", value);
					query.setParameter("newOrdinal", newOrdinal);
					query.executeUpdate();
				}
			}
		}
	}
	
	@Override
	public void delete(Issue issue) {
		super.delete(issue);
		listenerRegistry.post(new IssueDeleted(userManager.getCurrent(), issue));
	}

}
