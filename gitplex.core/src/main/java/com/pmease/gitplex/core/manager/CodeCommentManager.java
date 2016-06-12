package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;

public interface CodeCommentManager extends EntityDao<CodeComment> {
	
	Collection<CodeComment> query(Depot depot, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> query(Depot depot, ObjectId...commitIds);
	
	Collection<CodeComment> query(Depot depot, String... commitIds);
	
	void save(CodeComment comment);
	
	void delete(CodeComment comment);
	
	void test(Depot depot);
}
