package com.turbodev.server.manager;

import javax.annotation.Nullable;

import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.CodeCommentReply;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.PullRequestComment;
import com.turbodev.server.model.PullRequestStatusChange;

public interface UrlManager {
	
	String urlFor(Project project);
	
	String urlFor(PullRequest request);
	
	String urlFor(PullRequestComment comment);
	
	String urlFor(PullRequestStatusChange statusChange);
	
	String urlFor(CodeComment comment, @Nullable PullRequest request);

	String urlFor(CodeCommentReply reply, @Nullable PullRequest request);
	
}
