package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.match.WildcardUtils;

public class MilestoneCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String milestoneName;

	public MilestoneCriteria(String milestoneName) {
		this.milestoneName = milestoneName;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Issue> root, CriteriaBuilder builder) {
		Subquery<IssueSchedule> subQuery = query.subquery(IssueSchedule.class);
		Root<IssueSchedule> scheduleRoot = subQuery.from(IssueSchedule.class);
		subQuery.select(scheduleRoot);
		Join<?, ?> milestoneJoin = scheduleRoot.join(IssueSchedule.PROP_MILESTONE, JoinType.INNER);
		subQuery.where(builder.and(
				builder.equal(scheduleRoot.get(IssueSchedule.PROP_ISSUE), root), 
				builder.like(milestoneJoin.get(Milestone.PROP_NAME), milestoneName.replace("*", "%"))));
		return builder.exists(subQuery);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getSchedules().stream()
				.anyMatch(it->WildcardUtils.matchString(milestoneName, it.getMilestone().getName()));
	}

	@Override
	public String toStringWithoutParens() {
		return quote(IssueSchedule.NAME_MILESTONE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(milestoneName);
	}

	@Override
	public void fill(Issue issue) {
		Milestone milestone = issue.getProject().getHierarchyMilestone(milestoneName);
		if (milestone != null) {
			IssueSchedule schedule = new IssueSchedule();
			schedule.setIssue(issue);
			schedule.setMilestone(milestone);
			issue.getSchedules().add(schedule);
		}
	}

}
