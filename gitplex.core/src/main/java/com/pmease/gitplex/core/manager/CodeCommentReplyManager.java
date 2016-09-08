package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.PullRequest;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply, @Nullable PullRequest request, boolean callListeners);
	
}
