package com.pmease.gitplex.core.listener;

import com.pmease.gitplex.core.entity.CodeCommentReply;

public interface CodeCommentReplyListener {
	
	void onSaveReply(CodeCommentReply reply);
	
	void onDeleteReply(CodeCommentReply reply);
	
}
