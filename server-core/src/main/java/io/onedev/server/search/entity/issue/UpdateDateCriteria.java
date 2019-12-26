package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;

import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.IssueQueryConstants;

public class UpdateDateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public UpdateDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Path<Date> attribute = IssueQuery.getPath(root, IssueQueryConstants.ATTR_UPDATE_DATE);
		if (operator == IssueQueryLexer.IsBefore)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsBefore)
			return issue.getUpdateDate().before(date);
		else
			return issue.getUpdateDate().after(date);
	}

	@Override
	public String asString() {
		return quote(IssueQueryConstants.FIELD_UPDATE_DATE) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
