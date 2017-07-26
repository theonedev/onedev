package com.gitplex.server.manager;

import javax.annotation.Nullable;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentReply;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.model.PullRequestStatusChange;

public interface UrlManager {
	
	String urlFor(Project project);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestStatusChange statusChange);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

	String urlFor(CodeCommentReply reply, @Nullable PullRequest request);
	
}
