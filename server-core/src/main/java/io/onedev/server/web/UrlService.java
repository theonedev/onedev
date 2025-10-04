package io.onedev.server.web;

import io.onedev.server.model.*;
import io.onedev.server.util.ProjectAndRevision;
import org.eclipse.jgit.lib.ObjectId;

public interface UrlService {
	
	String urlForProject(Long projectId, boolean withRootUrl);
	
	String urlForProject(String projectPath, boolean withRootUrl);
	
	String urlFor(Project project, boolean withRootUrl);
	
	String urlFor(PullRequest request, boolean withRootUrl);
	
	String urlForPullRequest(Project project, Long pullRequestNumber, boolean withRootUrl);
	
	String urlFor(PullRequestComment comment, boolean withRootUrl);
	
	String urlFor(PullRequestChange change, boolean withRootUrl);
	
	String urlFor(Issue issue, boolean withRootUrl);
	
	String urlForIssue(Project project, Long issueNumber, boolean withRootUrl);
	
	String urlFor(Build build, boolean withRootUrl);

	String urlForBuild(Project project, Long buildNumber, boolean withRootUrl);
	
	String urlFor(IssueComment comment, boolean withRootUrl);
	
	String urlFor(IssueChange change, boolean withRootUrl);

	String urlForAttachment(Project project, String attachmentGroup, String attachmentName, boolean withRootUrl);
	
	String urlFor(CodeComment comment, boolean withRootUrl);

	String urlFor(Pack pack, boolean withRootUrl);
	
	String urlFor(CodeCommentReply reply, boolean withRootUrl);
	
	String urlFor(CodeCommentStatusChange change, boolean withRootUrl);
	
	String urlFor(ProjectAndRevision projectAndRevision, boolean withRootUrl);
	
	String urlFor(Project project, ObjectId commitId, boolean withRootUrl);
	
    String cloneUrlFor(Project project, boolean ssh);
    
}
