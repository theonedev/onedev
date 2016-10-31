package com.gitplex.core.manager;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.CodeComment;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestComment;
import com.gitplex.core.entity.PullRequestStatusChange;
import com.gitplex.core.entity.support.CodeCommentActivity;

public interface UrlManager {
	
	String urlFor(Account user);
	
	String urlFor(Depot depot);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestStatusChange statusChange);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

	String urlFor(CodeCommentActivity activity, @Nullable PullRequest request);
	
}
