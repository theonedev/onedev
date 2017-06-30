package com.gitplex.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;

public interface VisitManager {
	
	void visit(User user, PullRequest request);
	
	void visit(User user, CodeComment comment);
	
	@Nullable
	Date getVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getVisitDate(User user, CodeComment comment);
	
}
