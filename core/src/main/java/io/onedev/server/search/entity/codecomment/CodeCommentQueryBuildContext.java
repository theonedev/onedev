package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.search.entity.QueryBuildContext;

public class CodeCommentQueryBuildContext extends QueryBuildContext<CodeComment> {
	
	public CodeCommentQueryBuildContext(Root<CodeComment> root, CriteriaBuilder builder) {
		super(root, builder);
	}
	
	@Override
	public Join<?, ?> newJoin(String joinName) {
		return getRoot().join(joinName, JoinType.LEFT);
	}

}
