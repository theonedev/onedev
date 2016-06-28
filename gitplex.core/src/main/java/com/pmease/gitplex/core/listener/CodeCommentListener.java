package com.pmease.gitplex.core.listener;

import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;

public interface CodeCommentListener {
	
	void onSaveComment(CodeComment comment);
	
	void onDeleteComment(CodeComment comment);

	void onSaveCommentAndReply(CodeComment comment, CodeCommentReply reply);
}
