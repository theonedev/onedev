package com.gitplex.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;

public interface VisitInfoManager {
	
	void visit(Account user, Depot depot);
	
	void visit(Account user, PullRequest request);
	
	void visit(Account user, CodeComment comment);
	
	@Nullable
	Date getVisitDate(Account user, Depot depot);
	
	@Nullable
	Date getVisitDate(Account user, PullRequest request);
	
	@Nullable
	Date getVisitDate(Account user, CodeComment comment);
	
}
