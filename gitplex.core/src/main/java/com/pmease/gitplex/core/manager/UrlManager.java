package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface UrlManager {
	
	String urlFor(User user);
	
	String urlFor(Repository repository);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestCommentReply reply);
}
