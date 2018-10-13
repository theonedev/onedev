package io.onedev.server.web.component.markdown;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

public interface AtWhoReferenceSupport {
	
	List<PullRequest> findPullRequests(@Nullable Project project, String query, int count);

	List<Issue> findIssues(@Nullable Project project, String query, int count);
	
}
