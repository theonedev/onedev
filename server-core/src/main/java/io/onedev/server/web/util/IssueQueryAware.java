package io.onedev.server.web.util;

import javax.annotation.Nullable;

import io.onedev.server.search.entity.issue.IssueQuery;

public interface IssueQueryAware {

	@Nullable
	IssueQuery getIssueQuery();
	
}
