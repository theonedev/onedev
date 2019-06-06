package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class DescriptionCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<String> attribute = root.get(PullRequestConstants.ATTR_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return request.getDescription()!=null && request.getDescription().toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_DESCRIPTION) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " + PullRequestQuery.quote(value);
	}

}
