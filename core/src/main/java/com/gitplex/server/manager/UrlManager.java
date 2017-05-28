package com.gitplex.server.manager;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.support.CodeCommentActivity;

public interface UrlManager {
	
	String urlFor(Project project);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestStatusChange statusChange);
	
	String urlFor(CodeComment comment);

	String urlFor(CodeCommentActivity activity);
	
}
