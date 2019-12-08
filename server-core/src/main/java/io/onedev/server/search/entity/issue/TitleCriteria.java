package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.query.IssueQueryConstants;

public class TitleCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TitleCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Expression<String> attribute = root.get(IssueQueryConstants.ATTR_TITLE);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		String title = issue.getTitle();
		return title != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", title);
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueQueryConstants.FIELD_TITLE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " 
				+ IssueQuery.quote(value);
	}

}
