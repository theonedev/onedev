package io.onedev.server.search.entity.issue;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.query.IssueQueryConstants;

public class MilestoneCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String milestoneName;

	public MilestoneCriteria(String milestoneName) {
		this.milestoneName = milestoneName;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Path<String> attribute = root.join(IssueQueryConstants.ATTR_MILESTONE, JoinType.LEFT).get(Milestone.ATTR_NAME);
		String normalized = milestoneName.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Issue issue) {
		if (issue.getMilestone() != null)
			return WildcardUtils.matchString(milestoneName.toLowerCase(), issue.getMilestone().getName().toLowerCase());
		else 
			return false;
	}

	@Override
	public String asString() {
		return quote(IssueQueryConstants.FIELD_MILESTONE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(milestoneName);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setMilestone(issue.getProject().getMilestone(milestoneName));
	}

}
