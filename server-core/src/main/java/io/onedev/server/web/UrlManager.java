package io.onedev.server.web;

import io.onedev.server.model.*;
import io.onedev.server.util.ProjectAndRevision;
import org.eclipse.jgit.lib.ObjectId;

public interface UrlManager {
	
	String urlForProject(Long projectId);
	
	String urlForProject(String projectPath);
	
	String urlFor(Project project);
	
	String urlFor(PullRequest request);
	
	String urlForPullRequest(Project project, Long pullRequestNumber);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestChange change);
	
	String urlFor(Issue issue);
	
	String urlForIssue(Project project, Long issueNumber);
	
	String urlFor(Build build);

	String urlForBuild(Project project, Long buildNumber);
	
	String urlFor(IssueComment comment);
	
	String urlFor(IssueChange change);
	
	String urlFor(CodeComment comment);

	String urlFor(Pack pack);
	
	String urlFor(CodeCommentReply reply);
	
	String urlFor(CodeCommentStatusChange change);
	
	String urlFor(ProjectAndRevision projectAndRevision);
	
	String urlFor(Project project, ObjectId commitId);
	
    String cloneUrlFor(Project project, boolean ssh);
    
}
