package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.CommentReply;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface UrlManager {
	
	String urlFor(User user);
	
	String urlFor(Repository repository);
	
	String urlFor(PullRequest request);
	
	String urlFor(Comment comment);
	
	String urlFor(CommentReply reply);
}
