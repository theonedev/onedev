package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class UpdateDateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public UpdateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<Date> attribute = IssueQuery.getPath(root, IssueConstants.ATTR_UPDATE_DATE);
		if (operator == IssueQueryLexer.IsBefore)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		if (operator == IssueQueryLexer.IsBefore)
			return issue.getUpdateDate().before(value);
		else
			return issue.getUpdateDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueConstants.FIELD_UPDATE_DATE) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(rawValue);
	}

}
