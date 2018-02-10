package com.turbodev.server.web.component.markdown;

import java.util.List;

import com.turbodev.server.model.PullRequest;

public interface PullRequestReferenceSupport {
	
	List<PullRequest> findRequests(String query, int count);

}
