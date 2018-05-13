package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.QueryBuildContext;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private final IssueFieldManager issueFieldManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager issueFieldManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
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
	public void test() {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);
		Join<Issue, IssueField> join = root.join("fields");
		join.on(builder.equal(join.get("name"), "Type"));
		query.where(builder.or(builder.equal(join.get("value"), "Bug"), builder.equal(join.get("value"), "New Feature")));
		query.orderBy(builder.asc(join.get("ordinal")));
		Query<Issue> underlyingQuery = getSession().createQuery(query);
		underlyingQuery.setMaxResults(10);
		for (Issue issue: underlyingQuery.getResultList()) {
			System.out.println(issue.getId());
		}
		
		/*
		Project project = OneDev.getInstance(ProjectManager.class).load(1L);
		User user = OneDev.getInstance(UserManager.class).load(1L);
		IssueFieldManager issueFieldManager = OneDev.getInstance(IssueFieldManager.class);
		for (int i=1; i<=100000; i++) {
			Issue issue = new Issue();
			issue.setProject(project);
			issue.setTitle("issue " + i);
			issue.setReportDate(new Date());
			issue.setState("Open");
			issue.setReporter(user);
			issue.setVotes(0);
			save(issue);
			
			if (i % 3 == 0) {
				IssueField field = new IssueField();
				field.setIssue(issue);
				field.setName("Type");
				field.setOrdinal(0);
				field.setType("Choice");
				field.setValue("Feature");
				issueFieldManager.save(field);
				
				field = new IssueField();
				field.setIssue(issue);
				field.setName("Severity");
				field.setOrdinal(0);
				field.setType("Choice");
				field.setValue("Major");
				issueFieldManager.save(field);
			} else if (i % 3 == 1) {
				IssueField field = new IssueField();
				field.setIssue(issue);
				field.setName("Type");
				field.setOrdinal(1);
				field.setType("Choice");
				field.setValue("Bug");
				issueFieldManager.save(field);
				
				field = new IssueField();
				field.setIssue(issue);
				field.setName("Severity");
				field.setOrdinal(1);
				field.setType("Choice");
				field.setValue("Normal");
				issueFieldManager.save(field);
			} else {
				IssueField field = new IssueField();
				field.setIssue(issue);
				field.setName("Type");
				field.setOrdinal(2);
				field.setType("Choice");
				field.setValue("Task");
				issueFieldManager.save(field);
				
				field = new IssueField();
				field.setIssue(issue);
				field.setName("Severity");
				field.setOrdinal(2);
				field.setType("Choice");
				field.setValue("Minor");
				issueFieldManager.save(field);
			}
		}
		*/
	}

	@Transactional
	@Override
	public void save(Issue issue, Serializable fieldBean, Collection<String> promptedFields) {
		boolean isNew = issue.isNew();
		if (isNew)
			issue.setNumber(getNextNumber(issue.getProject()));
		save(issue);
		issueFieldManager.writeFields(issue, fieldBean, promptedFields);

		if (isNew)
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
	public long count(IssueCriteria issueCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Issue> root = criteriaQuery.from(Issue.class);
		
		if (issueCriteria != null)
			criteriaQuery.where(issueCriteria.getPredicate(new QueryBuildContext(root, builder)));
		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult();
	}

	@Sessional
	@Override
	public Collection<String> getUndefinedStates(Project project) {
		@SuppressWarnings("unchecked")
		Query<String> query = getSession().createQuery("select distinct state from Issue where project=:project");
		query.setParameter("project", project);
		
		List<String> states = query.getResultList();
		for (Iterator<String> it = states.iterator(); it.hasNext();) {
			if (project.getIssueWorkflow().getState(it.next()) != null)
				it.remove();
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

}
