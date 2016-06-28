package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.CodeCommentReply;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply);
	
	void delete(CodeCommentReply reply);
}
