package io.onedev.server.web;

import io.onedev.server.model.*;
import org.eclipse.jgit.lib.ObjectId;

public interface UrlManager {
	
	String urlForProject(Long projectId);
	
	String urlForProject(String projectPath);
	
	String urlFor(Project project);
	
	String urlFor(Project project, ObjectId commitId);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestChange change);
	
	String urlFor(Issue issue);
	
	String urlFor(Build build);
	
	String urlFor(IssueComment comment);
	
	String urlFor(IssueChange change);
	
	String urlFor(CodeComment comment);

	String urlFor(Pack pack);
	
	String urlFor(CodeCommentReply reply);
	
	String urlFor(CodeCommentStatusChange change);
	
    String cloneUrlFor(Project project, boolean ssh);
    
}
