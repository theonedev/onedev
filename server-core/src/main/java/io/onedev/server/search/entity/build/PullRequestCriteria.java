package io.onedev.server.search.entity.build;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class PullRequestCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final PullRequest request;
	
	private final String value;
	
	public PullRequestCriteria(@Nullable Project project, String value) {
		request = EntityQuery.getPullRequest(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<PullRequest> attribute = from.get(Build.PROP_PULL_REQUEST);
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
