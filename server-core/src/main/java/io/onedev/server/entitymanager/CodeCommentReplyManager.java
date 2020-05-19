package io.onedev.server.entitymanager;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply);
	
}	
