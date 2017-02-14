package com.gitplex.server.core.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.CodeComment;
import com.gitplex.server.core.entity.CodeCommentStatusChange;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.event.codecomment.CodeCommentEvent;

public interface CodeCommentManager extends EntityManager<CodeComment> {
	
	Collection<CodeComment> findAll(Depot depot, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> findAll(Depot depot, ObjectId...commitIds);
	
	void changeStatus(CodeCommentStatusChange statusChange);
	
	@Nullable
	CodeComment find(String uuid);
	
	List<CodeComment> findAllAfter(Depot depot, @Nullable String commentUUID);
	
	void save(CodeComment comment);
	
	void sendNotifications(CodeCommentEvent event);
	
}