package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;

public interface UrlManager {
	
	String urlFor(Project project);
	
	String urlFor(Project project, ObjectId commitId);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestChange change);
	
	String urlFor(Issue issue);
	
	String urlFor(Build build);
	
	String urlFor(IssueComment comment);
	
	String urlFor(IssueChange change);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

	String urlFor(CodeCommentReply reply, @Nullable PullRequest request);

    String cloneUrlFor(Project project, boolean ssh);
    
}
