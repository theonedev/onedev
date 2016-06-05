package com.pmease.gitplex.core.listener;

import com.pmease.gitplex.core.entity.CodeComment;

public interface CodeCommentListener {
	
	void onSaveComment(CodeComment comment);
	
	void onDeleteComment(CodeComment comment);
	
}
