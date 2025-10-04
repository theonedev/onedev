package io.onedev.server.service;

import java.util.Date;
import java.util.List;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;

public interface CodeCommentReplyService extends EntityService<CodeCommentReply> {

	void create(CodeCommentReply reply);

	void update(CodeCommentReply reply);
	
 	List<CodeCommentReply> query(User creator, Date fromDate, Date toDate);

}	
