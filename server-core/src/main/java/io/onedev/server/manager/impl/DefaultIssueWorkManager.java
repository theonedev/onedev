package io.onedev.server.manager.impl;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Arrays;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueWorkManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import org.hibernate.query.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.model.IssueWork.PROP_DAY;
import static io.onedev.server.model.IssueWork.PROP_ISSUE;
import static io.onedev.server.model.Project.PROP_TIME_TRACKING;
import static java.util.stream.Collectors.toSet;

@Singleton
public class DefaultIssueWorkManager extends BaseEntityManager<IssueWork> implements IssueWorkManager {

	private final IssueManager issueManager;
	
	private final IssueFieldManager issueFieldManager;
	
	@Inject
    public DefaultIssueWorkManager(Dao dao, IssueManager issueManager, IssueFieldManager issueFieldManager) {
        super(dao);
		this.issueManager = issueManager;
		this.issueFieldManager = issueFieldManager;
    }

	@Override
	public void create(IssueWork work) {
		Preconditions.checkState(work.isNew());
		dao.persist(work);
	}

	@Override
	public void update(IssueWork work) {
		Preconditions.checkState(!work.isNew());
		dao.persist(work);
	}

	@Sessional
	@Override
	public List<IssueWork> query(ProjectScope projectScope, EntityQuery<Issue> issueQuery, long fromDay, long toDay) {
		var builder = getSession().getCriteriaBuilder();
		var criteriaQuery = builder.createQuery(IssueWork.class);
		var root = criteriaQuery.from(IssueWork.class);
		Join<Issue, Issue> issue = root.join(PROP_ISSUE, JoinType.INNER);
		var issuePredicates = issueManager.buildPredicates(projectScope, issueQuery.getCriteria(), 
				criteriaQuery, builder, issue);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.addAll(Arrays.asList(issuePredicates));
		
		Join<Project, Project> project = issue.join(Issue.PROP_PROJECT, JoinType.INNER);
		predicates.add(builder.equal(project.get(PROP_TIME_TRACKING), true));
		predicates.add(builder.ge(root.get(PROP_DAY), fromDay));
		predicates.add(builder.le(root.get(PROP_DAY), toDay));
		
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(issueManager.buildOrders(issueQuery.getSorts(), builder, issue));

		Query<IssueWork> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(Integer.MAX_VALUE);
		var works = query.getResultList();
		issueFieldManager.populateFields(works.stream().map(IssueWork::getIssue).collect(toSet()));
		
		return works;
	}

}