package com.gitplex.server.web.component.markdown;

import java.util.List;

import com.gitplex.server.model.PullRequest;

public interface PullRequestReferenceSupport {
	
	List<PullRequest> findRequests(String query, int count);

}
