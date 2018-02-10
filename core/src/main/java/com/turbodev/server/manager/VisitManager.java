package com.turbodev.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;

public interface VisitManager {
	
	void visitPullRequest(User user, PullRequest request);
	
	void visitPullRequestCodeComments(User user, PullRequest request);
	
	void visitCodeComment(User user, CodeComment comment);
	
	@Nullable
	Date getPullRequestVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getCodeCommentVisitDate(User user, CodeComment comment);
	
}
