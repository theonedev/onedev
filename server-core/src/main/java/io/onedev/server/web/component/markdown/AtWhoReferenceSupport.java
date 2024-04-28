package io.onedev.server.web.component.markdown;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

import java.io.Serializable;
import java.util.List;

public interface AtWhoReferenceSupport extends Serializable {
	
	Project getCurrentProject();
	
	List<PullRequest> queryPullRequests(Project project, String query, int count);

	List<Issue> queryIssues(Project project, String query, int count);
	
	List<Build> queryBuilds(Project project, String query, int count);
	
}
