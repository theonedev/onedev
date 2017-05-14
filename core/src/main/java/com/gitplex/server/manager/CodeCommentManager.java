package com.gitplex.server.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.Depot;
import com.gitplex.server.persistence.dao.EntityManager;

public interface CodeCommentManager extends EntityManager<CodeComment> {
	
	Collection<CodeComment> findAll(Depot depot, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> findAll(Depot depot, ObjectId...commitIds);
	
	void changeStatus(CodeCommentStatusChange statusChange);
	
	@Nullable
	CodeComment find(String uuid);
	
	List<CodeComment> findAllAfter(Depot depot, @Nullable String commentUUID);
	
	@Override
	void save(CodeComment comment);
	
}