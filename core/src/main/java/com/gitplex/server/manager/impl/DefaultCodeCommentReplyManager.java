package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.codecomment.CodeCommentReplied;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentReplyManager;
import com.gitplex.server.model.CodeCommentReply;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.CompareContext;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultCodeCommentReplyManager extends AbstractEntityManager<CodeCommentReply> 
		implements CodeCommentReplyManager {

	private final ListenerRegistry listenerRegistry;
	
	private final CodeCommentManager codeCommentManager;
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, ListenerRegistry listenerRegistry, 
			CodeCommentManager codeCommentManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.codeCommentManager = codeCommentManager;
	}

	@Transactional
	@Override
	public void save(CodeCommentReply reply, CompareContext compareContext, PullRequest request) {
		boolean isNew = reply.isNew();
		dao.persist(reply);
		if (isNew) {
			CodeCommentReplied event = new CodeCommentReplied(reply, request); 
			reply.getComment().setCompareContext(compareContext);
			reply.getComment().setLastEvent(event);
			listenerRegistry.post(event);
			codeCommentManager.save(reply.getComment());
		}
	}
	
}
