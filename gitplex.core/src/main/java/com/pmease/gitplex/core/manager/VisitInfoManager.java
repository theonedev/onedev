package com.pmease.gitplex.core.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;

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
