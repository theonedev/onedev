package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.PullRequest;

public interface CommentReplyManager extends EntityDao<CommentReply> {

	void save(CommentReply reply);
	
	Collection<CommentReply> findBy(PullRequest request);
}
