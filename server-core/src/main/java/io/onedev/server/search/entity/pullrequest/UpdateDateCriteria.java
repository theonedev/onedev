package io.onedev.server.search.entity.pullrequest;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class UpdateDateCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public UpdateDateCriteria(String value, int operator) {
		this.operator = operator;
		this.value = value;
		date = EntityQuery.getDateValue(value);
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<Date> attribute = PullRequestQuery.getPath(root, PullRequest.PROP_UPDATE_DATE);
		if (operator == PullRequestQueryLexer.IsBefore)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.IsBefore)
			return request.getUpdateDate().before(date);
		else
			return request.getUpdateDate().after(date);
	}

	@Override
	public String asString() {
		return quote(PullRequest.FIELD_UPDATE_DATE) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
