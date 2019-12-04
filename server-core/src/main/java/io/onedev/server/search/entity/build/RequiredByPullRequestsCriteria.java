package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequestBuild;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.BuildQueryConstants;

public class RequiredByPullRequestsCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		From<?, ?> join = root.join(BuildQueryConstants.ATTR_PULL_REQUEST_BUILDS, JoinType.LEFT);
		return builder.equal(join.get(PullRequestBuild.ATTR_REQUIRED), true); 
	}

	@Override
	public boolean matches(Build build) {
		return build.getPullRequestBuilds().stream().anyMatch(it->it.isRequired());
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.RequiredByPullRequests);
	}

}
