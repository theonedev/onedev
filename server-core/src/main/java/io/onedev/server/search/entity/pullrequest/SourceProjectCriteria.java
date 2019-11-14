package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class SourceProjectCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final Project value;
	
	public SourceProjectCriteria(Project value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = root.get(PullRequestConstants.ATTR_SOURCE_PROJECT);
		return builder.equal(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return Objects.equals(request.getSourceProject(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_SOURCE_PROJECT) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " + PullRequestQuery.quote(value.getName());
	}

}
