package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.util.query.QueryBuildContext;

public class IssueQueryBuildContext extends QueryBuildContext<Issue> {
	
	public IssueQueryBuildContext(Root<Issue> root, CriteriaBuilder builder) {
		super(root, builder);
	}

	@Override
	public Join<?, ?> newJoin(String joinPath) {
		if (joinPath.equals(Issue.FIELD_MILESTONE)) {
			return getRoot().join("milestone", JoinType.LEFT);
		} else {
			Join<Issue, ?> join = getRoot().join("fieldUnaries", JoinType.LEFT);
			join.on(getBuilder().equal(join.get("name"), joinPath));
			return join;
		}
	}

}
