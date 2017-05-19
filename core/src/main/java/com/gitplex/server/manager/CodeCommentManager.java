package com.gitplex.server.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.EntityManager;

public interface CodeCommentManager extends EntityManager<CodeComment> {
	
	Collection<CodeComment> findAll(PullRequest request, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> findAll(PullRequest request, ObjectId...commitIds);
	
	void changeStatus(CodeCommentStatusChange statusChange);
	
	@Override
	void save(CodeComment comment);
	
}