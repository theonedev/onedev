package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.commons.utils.match.WildcardUtils;

public class MilestoneCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final String milestoneName;
	
	private final int operator;

	public MilestoneCriteria(String milestoneName, int operator) {
		this.milestoneName = milestoneName;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueSchedule> scheduleQuery = query.subquery(IssueSchedule.class);
		Root<IssueSchedule> schedule = scheduleQuery.from(IssueSchedule.class);
		scheduleQuery.select(schedule);
		Join<?, ?> milestoneJoin = schedule.join(IssueSchedule.PROP_MILESTONE, JoinType.INNER);
		scheduleQuery.where(builder.and(
				builder.equal(schedule.get(IssueSchedule.PROP_ISSUE), from), 
				builder.like(milestoneJoin.get(Milestone.PROP_NAME), milestoneName.replace("*", "%"))));
		var predicate =  builder.exists(scheduleQuery);
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		var matches = issue.getSchedules().stream()
				.anyMatch(it->WildcardUtils.matchString(milestoneName, it.getMilestone().getName()));
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(IssueSchedule.NAME_MILESTONE) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(milestoneName);
	}

	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is) {
			Milestone milestone = issue.getProject().getHierarchyMilestone(milestoneName);
			if (milestone != null) {
				IssueSchedule schedule = new IssueSchedule();
				schedule.setIssue(issue);
				schedule.setMilestone(milestone);
				issue.getSchedules().add(schedule);
			}
		}
	}

}
