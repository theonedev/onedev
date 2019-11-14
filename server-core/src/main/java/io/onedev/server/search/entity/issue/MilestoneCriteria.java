package io.onedev.server.search.entity.issue;

import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class MilestoneCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String milestoneName;

	public MilestoneCriteria(@Nullable String milestoneName) {
		this.milestoneName = milestoneName;
	}

	public String getValue() {
		return milestoneName;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = root.join(IssueConstants.ATTR_MILESTONE, JoinType.LEFT).get(Milestone.ATTR_NAME);
		if (milestoneName != null)
			return builder.equal(attribute, milestoneName);
		else
			return builder.isNull(attribute);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		if (milestoneName != null)
			return issue.getMilestone() != null && issue.getMilestone().getName().equals(milestoneName);
		else
			return issue.getMilestone() == null;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		if (milestoneName != null) {
			return IssueQuery.quote(IssueConstants.FIELD_MILESTONE) + " " 
					+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
					+ IssueQuery.quote(milestoneName);
		} else {
			return IssueQuery.quote(IssueConstants.FIELD_MILESTONE) + " " 
					+ IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
		}
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (milestoneName != null)
			issue.setMilestone(issue.getProject().getMilestone(milestoneName));
	}

}
