package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.util.criteria.Criteria;

public class MilestoneIsEmptyCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		return builder.isEmpty(from.get(Issue.PROP_SCHEDULES));
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
