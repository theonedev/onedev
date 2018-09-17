package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.search.entity.QueryBuildContext;

public class BuildQueryBuildContext extends QueryBuildContext<Build> {
	
	public BuildQueryBuildContext(Root<Build> root, CriteriaBuilder builder) {
		super(root, builder);
	}
	
	@Override
	public Join<?, ?> newJoin(String joinName) {
		return getRoot().join(joinName, JoinType.LEFT);
	}

}
