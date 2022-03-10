package io.onedev.server.search.entity.build;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class InPipelineOfCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	public InPipelineOfCriteria(@Nullable Project project, String value) {
		build = EntityQuery.getBuild(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		return builder.equal(from.get(Build.PROP_PIPELINE), build.getPipeline());
	}

	@Override
	public boolean matches(Build build) {
		return build.getPipeline().equals(this.build.getPipeline());
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.InPipelineOf) + " " + quote(value);
	}

}
