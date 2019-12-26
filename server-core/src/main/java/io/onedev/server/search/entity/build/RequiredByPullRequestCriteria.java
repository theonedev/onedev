package io.onedev.server.search.entity.build;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.BuildQueryConstants;

public class RequiredByPullRequestCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final PullRequest request;
	
	private final String value;
	
	public RequiredByPullRequestCriteria(@Nullable Project project, String value) {
		request = EntityQuery.getPullRequest(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(BuildQueryConstants.ATTR_PULL_REQUEST_BUILDS, JoinType.LEFT);
		join.on(builder.and(
				builder.equal(join.get(PullRequestBuild.ATTR_REQUEST), request), 
				builder.equal(join.get(PullRequestBuild.ATTR_REQUIRED), true)));
		return builder.equal(root.get(BuildQueryConstants.ATTR_PROJECT), request.getTargetProject());
	}

	@Override
	public boolean matches(Build build) {
		return build.getProject().equals(request.getTargetProject()) 
				&& build.getPullRequestBuilds().stream().anyMatch(it -> it.getRequest().equals(request) && it.isRequired());
	}

	@Override
	public String asString() {
		return BuildQuery.getRuleName(BuildQueryLexer.RequiredByPullRequest) + " " + quote(value);
	}

}
