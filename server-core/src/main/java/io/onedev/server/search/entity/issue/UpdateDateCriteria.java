package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.DateUtils;

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

	public UpdateDateCriteria(Date date, int operator) {
		this.date = date;
		this.operator = operator;
		this.value = DateUtils.formatDate(date);
	}
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Path<Date> attribute = IssueQuery.getPath(root, Issue.PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE);
		if (operator == IssueQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsUntil)
			return issue.getLastUpdate().getDate().before(date);
		else
			return issue.getLastUpdate().getDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_UPDATE_DATE) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
