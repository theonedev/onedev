package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;

public class MilestoneIsEmptyCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		return builder.isNull(root.join(Issue.PROP_MILESTONE, JoinType.LEFT));
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getMilestone() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_MILESTONE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
	}

}
