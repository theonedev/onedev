package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.CriteriaContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.PathContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.QueryContext;

public class CommitQueryBuilder extends CommitQueryBaseVisitor<CommitQuery> {

	@Override
	public CommitQuery visitCriteria(CriteriaContext ctx) {
		return super.visitCriteria(ctx);
	}

	@Override
	public CommitQuery visitPath(PathContext ctx) {
		// TODO Auto-generated method stub
		return super.visitPath(ctx);
	}

	@Override
	public CommitQuery visitQuery(QueryContext ctx) {
		return super.visitQuery(ctx);
	}

}
