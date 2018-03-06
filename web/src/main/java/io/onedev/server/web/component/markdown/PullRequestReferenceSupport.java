package io.onedev.server.web.component.markdown;

import java.util.List;

import io.onedev.server.model.PullRequest;

public interface PullRequestReferenceSupport {
	
	List<PullRequest> findRequests(String query, int count);

}
