package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;

public interface UrlManager {
	
	String urlFor(Account user);
	
	String urlFor(Depot depot);
	
	String urlFor(PullRequest request);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

}
