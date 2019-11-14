package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class RequiredByPullRequestsCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder, User user) {
		From<?, ?> join = root.join(BuildConstants.ATTR_PULL_REQUEST_BUILDS, JoinType.LEFT);
		return builder.equal(join.get(PullRequestBuild.ATTR_REQUIRED), true); 
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getPullRequestBuilds().stream().anyMatch(it->it.isRequired());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.RequiredByPullRequests);
	}

}
