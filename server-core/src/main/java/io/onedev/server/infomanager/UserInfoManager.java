package io.onedev.server.infomanager;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public interface UserInfoManager {
	
	void visitPullRequest(User user, PullRequest request);
	
	void visitPullRequestCodeComments(User user, PullRequest request);
	
	void visitIssue(User user, Issue issue);
	
	void visitCodeComment(User user, CodeComment comment);
	
	@Nullable
	Date getIssueVisitDate(User user, Issue issue);
	
	@Nullable
	Date getPullRequestVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getCodeCommentVisitDate(User user, CodeComment comment);
	
}
