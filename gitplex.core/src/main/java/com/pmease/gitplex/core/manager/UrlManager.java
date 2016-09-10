package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.entity.support.CodeCommentActivity;

public interface UrlManager {
	
	String urlFor(Account user);
	
	String urlFor(Depot depot);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestStatusChange statusChange);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

	String urlFor(CodeCommentActivity activity, @Nullable PullRequest request);
	
}
