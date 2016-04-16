package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Comment;

public interface CommentManager extends EntityDao<Comment> {

	void save(Comment comment, boolean notify);
	
}
