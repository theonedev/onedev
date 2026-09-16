package io.onedev.server.search.entity.pullrequest;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.QueryUtils;
import io.onedev.server.util.criteria.Criteria;

public class SubmitDateCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date date;
	
	private final String value;
	
	public SubmitDateCriteria(String value, int operator) {
		this.operator = operator;
		this.value = value;
		date = QueryUtils.getDateValue(value);
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Date> attribute = from.get(PullRequest.PROP_SUBMIT_DATE);
		if (operator == PullRequestQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.IsUntil)
			return request.getSubmitDate().before(date);
		else
			return request.getSubmitDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_SUBMIT_DATE) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
