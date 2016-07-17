package com.pmease.gitplex.core.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;

public interface CodeCommentManager extends EntityManager<CodeComment> {
	
	Collection<CodeComment> findAll(Depot depot, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> findAll(Depot depot, ObjectId...commitIds);
	
	void save(CodeComment comment);
	
	void toggleResolve(CodeComment comment, CodeCommentReply reply);
	
	void toggleResolve(CodeComment comment, Account user);
	
	void delete(CodeComment comment);
	
	@Nullable
	CodeComment find(String uuid);
	
	List<CodeComment> findAllAfter(Depot depot, @Nullable String commentUUID);
}
