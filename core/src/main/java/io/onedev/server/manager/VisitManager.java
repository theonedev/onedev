package io.onedev.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public interface VisitManager {
	
	void visitPullRequest(User user, PullRequest request);
	
	void visitPullRequest(User user, PullRequest request, Date date);
	
	void visitPullRequestCodeComments(User user, PullRequest request);
	
	void visitPullRequestCodeComments(User user, PullRequest request, Date date);
	
	void visitIssue(User user, Issue issue);
	
	void visitIssue(User user, Issue issue, Date date);
	
	void visitCodeComment(User user, CodeComment comment);
	
	void visitCodeComment(User user, CodeComment comment, Date date);
	
	@Nullable
	Date getIssueVisitDate(User user, Issue issue);
	
	@Nullable
	Date getPullRequestVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getCodeCommentVisitDate(User user, CodeComment comment);
	
}
