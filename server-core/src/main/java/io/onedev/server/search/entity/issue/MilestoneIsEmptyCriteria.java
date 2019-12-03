package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.query.IssueQueryConstants;

public class MilestoneIsEmptyCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		return builder.isNull(root.join(IssueQueryConstants.ATTR_MILESTONE, JoinType.LEFT));
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return issue.getMilestone() == null;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueQueryConstants.FIELD_MILESTONE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
	}

}
