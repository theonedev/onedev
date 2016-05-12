package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;

public interface CodeCommentManager extends EntityDao<CodeComment> {
	
	Collection<CodeComment> query(Depot depot, String commit, @Nullable String path);
	
}
