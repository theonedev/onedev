package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;

public class LastActivityDateCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public LastActivityDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	public LastActivityDateCriteria(Date date, int operator) {
		this.date = date;
		this.operator = operator;
		this.value = DateUtils.formatDate(date);
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Date> attribute = IssueQuery.getPath(from, Issue.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE);
		if (operator == IssueQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsUntil)
			return issue.getLastActivity().getDate().before(date);
		else
			return issue.getLastActivity().getDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_LAST_ACTIVITY_DATE) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
