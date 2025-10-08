package io.onedev.server.web.util;

import org.jspecify.annotations.Nullable;

import io.onedev.server.search.entity.issue.IssueQuery;

public interface IssueQueryAware {

	@Nullable
	IssueQuery getIssueQuery();
	
}
