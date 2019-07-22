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

public class CloseDateCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public CloseDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<Date> attribute = PullRequestQuery.getPath(root, PullRequestConstants.ATTR_CLOSE_DATE);
		if (operator == PullRequestQueryLexer.IsBefore)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		if (request.getCloseInfo() != null) {
			if (operator == PullRequestQueryLexer.IsBefore)
				return request.getCloseInfo().getDate().before(value);
			else
				return request.getCloseInfo().getDate().after(value);
		} else {
			return false;
		}
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_CLOSE_DATE) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(rawValue);
	}

}
