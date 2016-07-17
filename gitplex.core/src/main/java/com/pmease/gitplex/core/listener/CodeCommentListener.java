package com.pmease.gitplex.core.listener;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;

public interface CodeCommentListener {
	
	void onComment(CodeComment comment);
	
	void onReplyComment(CodeCommentReply reply);
	
	void onToggleResolve(CodeComment comment, Account user);
	
}
