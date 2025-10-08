package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PullRequestCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final PullRequest request;
	
	private final String value;
	
	private final int operator;
	
	public PullRequestCriteria(@Nullable Project project, String value, int operator) {
		request = EntityQuery.getPullRequest(project, value);
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<PullRequest> attribute = from.get(Build.PROP_PULL_REQUEST);
		var predicate = builder.equal(attribute, request);
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getProject().equals(request.getTargetProject()) && request.equals(build.getRequest());
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
