package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;

public class MilestoneIsEmptyCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Issue> root, CriteriaBuilder builder) {
		return builder.isEmpty(root.get(Issue.PROP_SCHEDULES));
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getSchedules().isEmpty();
	}

	@Override
	public String toStringWithoutParens() {
		return quote(IssueSchedule.NAME_MILESTONE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
	}

}
