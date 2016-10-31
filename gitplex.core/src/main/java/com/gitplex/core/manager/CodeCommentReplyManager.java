package com.gitplex.core.manager;

import com.gitplex.core.entity.CodeCommentReply;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply, boolean callListeners);
	
}
