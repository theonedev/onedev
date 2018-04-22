package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;

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
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.UndefinedStateResolution;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private final IssueFieldManager issueFieldManager;
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager issueFieldManager) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
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
	public void save(Issue issue, Serializable fieldBean, Collection<String> fieldNames) {
		save(issue);
		issueFieldManager.writeFields(issue, fieldBean, fieldNames);
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
	
}
