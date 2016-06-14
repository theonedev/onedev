package com.pmease.gitplex.core.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;

public interface CodeCommentManager extends EntityDao<CodeComment> {
	
	Collection<CodeComment> query(Depot depot, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> query(Depot depot, ObjectId...commitIds);
	
	void save(CodeComment comment);
	
	void delete(CodeComment comment);
	
	@Nullable
	CodeComment find(String uuid);
	
	List<CodeComment> queryAfter(Depot depot, @Nullable String commentUUID);
}
