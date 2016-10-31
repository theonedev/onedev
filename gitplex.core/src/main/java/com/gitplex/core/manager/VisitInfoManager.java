package com.gitplex.core.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.CodeComment;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;

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
