package io.onedev.server.entitymanager;

import java.util.Date;
import java.util.List;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void create(CodeCommentReply reply);

	void update(CodeCommentReply reply);
	
 	List<CodeCommentReply> query(User creator, Date fromDate, Date toDate);

}	
