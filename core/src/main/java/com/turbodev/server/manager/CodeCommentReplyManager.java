package com.turbodev.server.manager;

import com.turbodev.server.model.CodeCommentReply;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.support.CompareContext;
import com.turbodev.server.persistence.dao.EntityManager;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply, CompareContext compareContext, PullRequest request);
	
}
