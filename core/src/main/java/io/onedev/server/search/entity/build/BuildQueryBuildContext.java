package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.BuildConstants;

public class BuildQueryBuildContext extends QueryBuildContext<Build> {
	
	public BuildQueryBuildContext(Root<Build> root, CriteriaBuilder builder) {
		super(root, builder);
	}
	
	@Override
	public Join<?, ?> newJoin(String joinName) {
		switch (joinName) {
		case BuildConstants.FIELD_DEPENDENCIES:
			return getRoot().join(BuildConstants.ATTR_DEPENDENCIES, JoinType.LEFT);
		case BuildConstants.FIELD_DEPENDENTS:
			return getRoot().join(BuildConstants.ATTR_DEPENDENTS, JoinType.LEFT);
		default:
			Join<Build, ?> join = getRoot().join(BuildConstants.ATTR_PARAMS, JoinType.LEFT);
			join.on(getBuilder().equal(join.get(BuildParam.ATTR_NAME), joinName));
			return join;
		}
	}		

}
