package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;
import java.util.Date;

public class CloseDateCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date date;
	
	private final String value;
	
	public CloseDateCriteria(String value, int operator) {
		this.operator = operator;
		this.value = value;
		date = EntityQuery.getDateValue(value);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Date> attribute = from.get(PullRequest.PROP_CLOSE_DATE);
		if (operator == PullRequestQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.IsUntil)
			return request.getCloseDate().before(date);
		else
			return request.getCloseDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_CLOSE_DATE) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
