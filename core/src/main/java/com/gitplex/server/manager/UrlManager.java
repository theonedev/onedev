package com.gitplex.server.manager;

import javax.annotation.Nullable;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.CodeComment;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestComment;
import com.gitplex.server.entity.PullRequestStatusChange;
import com.gitplex.server.entity.support.CodeCommentActivity;

public interface UrlManager {
	
	String urlFor(Account user);
	
	String urlFor(Depot depot);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestStatusChange statusChange);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

	String urlFor(CodeCommentActivity activity, @Nullable PullRequest request);
	
}
