package io.onedev.server.entityquery.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;

public class CodeCommentQueryBuildContext extends QueryBuildContext<CodeComment> {
	
	public CodeCommentQueryBuildContext(Root<CodeComment> root, CriteriaBuilder builder) {
		super(root, builder);
	}
	
	@Override
	public Join<?, ?> newJoin(String joinName) {
		return getRoot().join(joinName, JoinType.LEFT);
	}

}
