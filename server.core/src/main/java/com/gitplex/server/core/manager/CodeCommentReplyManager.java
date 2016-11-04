package com.gitplex.server.core.manager;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.CodeCommentReply;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply, boolean callListeners);
	
}
