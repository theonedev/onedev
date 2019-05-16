package io.onedev.server.search.entity.pullrequest;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class SubmitDateCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public SubmitDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<Date> attribute = root.get(PullRequestConstants.ATTR_SUBMIT_DATE);
		if (operator == PullRequestQueryLexer.IsBefore)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		if (operator == PullRequestQueryLexer.IsBefore)
			return request.getSubmitDate().before(value);
		else
			return request.getSubmitDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_SUBMIT_DATE) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(rawValue);
	}

}
