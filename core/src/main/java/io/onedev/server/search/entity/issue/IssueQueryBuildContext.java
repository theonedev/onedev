package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.model.IssueFieldUnary;

public class IssueQueryBuildContext extends QueryBuildContext<Issue> {
	
	public IssueQueryBuildContext(Root<Issue> root, CriteriaBuilder builder) {
		super(root, builder);
	}

	@Override
	public Join<?, ?> newJoin(String joinName) {
		switch (joinName) {
		case IssueConstants.FIELD_COMMENT:
			return getRoot().join(IssueConstants.ATTR_COMMENTS, JoinType.LEFT);
		case IssueConstants.FIELD_MILESTONE:
			return getRoot().join(IssueConstants.ATTR_MILESTONE, JoinType.LEFT);
		default:
			Join<Issue, ?> join = getRoot().join(IssueConstants.ATTR_FIELD_UNARIES, JoinType.LEFT);
			join.on(getBuilder().equal(join.get(IssueFieldUnary.FIELD_ATTR_NAME), joinName));
			return join;
		}
	}

}
