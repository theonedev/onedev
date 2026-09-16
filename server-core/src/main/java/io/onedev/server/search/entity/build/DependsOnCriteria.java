package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.QueryUtils;
import io.onedev.server.util.criteria.Criteria;

public class DependsOnCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	public DependsOnCriteria(@Nullable Project project, String value) {
		build = QueryUtils.getBuild(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(Build.PROP_DEPENDENCIES, JoinType.LEFT);
		join.on(builder.equal(join.get(BuildDependence.PROP_DEPENDENCY), build));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Build build) {
		for (BuildDependence dependence: this.build.getDependents()) {
			if (dependence.getDependent().equals(build))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.DependsOn) + " " + quote(value);
	}

}
