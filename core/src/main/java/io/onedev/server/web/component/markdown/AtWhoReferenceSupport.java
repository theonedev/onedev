package io.onedev.server.web.component.markdown;

import java.util.List;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

public interface AtWhoReferenceSupport {
	
	List<PullRequest> findPullRequests(String query, int count);

	List<Issue> findIssues(String query, int count);
	
}
