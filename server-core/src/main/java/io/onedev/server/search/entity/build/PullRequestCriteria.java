package io.onedev.server.search.entity.build;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class PullRequestCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final PullRequest request;
	
	private final String value;
	
	public PullRequestCriteria(@Nullable Project project, String value) {
		request = EntityQuery.getPullRequest(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<PullRequest> attribute = root.get(Build.PROP_PULL_REQUEST);
		return builder.equal(attribute, request);
	}

	@Override
	public boolean matches(Build build) {
		return build.getProject().equals(request.getTargetProject()) && request.equals(build.getRequest());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ quote(value);
	}

}
