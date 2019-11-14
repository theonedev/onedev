package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class TitleCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TitleCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<String> attribute = root.get(IssueConstants.ATTR_TITLE);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return issue.getTitle().toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueConstants.FIELD_TITLE) + " " + IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " + IssueQuery.quote(value);
	}

}
