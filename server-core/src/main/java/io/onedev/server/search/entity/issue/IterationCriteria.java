package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class IterationCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final String iterationName;
	
	private final int operator;

	public IterationCriteria(String iterationName, int operator) {
		this.iterationName = iterationName;
		this.operator = operator;
	}

	public String getIterationName() {
		return iterationName;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueSchedule> scheduleQuery = query.subquery(IssueSchedule.class);
		Root<IssueSchedule> schedule = scheduleQuery.from(IssueSchedule.class);
		scheduleQuery.select(schedule);
		Join<?, ?> iterationJoin = schedule.join(IssueSchedule.PROP_ITERATION, JoinType.INNER);
		scheduleQuery.where(builder.and(
				builder.equal(schedule.get(IssueSchedule.PROP_ISSUE), from), 
				builder.like(builder.lower(iterationJoin.get(Iteration.PROP_NAME)), iterationName.toLowerCase().replace("*", "%"))));
		var predicate =  builder.exists(scheduleQuery);
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		var matches = issue.getSchedules().stream()
				.anyMatch(it->WildcardUtils.matchString(iterationName.toLowerCase(), it.getIteration().getName().toLowerCase()));
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(IssueSchedule.NAME_ITERATION) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(iterationName);
	}

	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is) {
			Iteration iteration = issue.getProject().getHierarchyIteration(iterationName);
			if (iteration != null) {
				IssueSchedule schedule = new IssueSchedule();
				schedule.setIssue(issue);
				schedule.setIteration(iteration);
				issue.getSchedules().add(schedule);
			}
		}
	}

}
