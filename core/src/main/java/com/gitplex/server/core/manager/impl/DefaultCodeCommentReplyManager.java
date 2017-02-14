package com.gitplex.server.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.calla.loader.ListenerRegistry;
import com.gitplex.server.core.entity.CodeCommentReply;
import com.gitplex.server.core.event.codecomment.CodeCommentReplied;
import com.gitplex.server.core.manager.CodeCommentManager;
import com.gitplex.server.core.manager.CodeCommentReplyManager;

@Singleton
public class DefaultCodeCommentReplyManager extends AbstractEntityManager<CodeCommentReply> 
		implements CodeCommentReplyManager {

	private final ListenerRegistry listenerRegistry;
	
	private final CodeCommentManager codeCommentManager;
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, ListenerRegistry listenerRegistry, CodeCommentManager codeCommentManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.codeCommentManager = codeCommentManager;
	}

	@Transactional
	@Override
	public void save(CodeCommentReply reply, boolean callListeners) {
		boolean isNew = reply.isNew();
		dao.persist(reply);
		if (isNew && callListeners) {
			CodeCommentReplied event = new CodeCommentReplied(reply); 
			listenerRegistry.post(event);
			reply.getComment().setLastEvent(event);
			codeCommentManager.save(reply.getComment());
		}
	}
	
	@Transactional
	@Override
	public void save(CodeCommentReply reply) {
		save(reply, true);
	}
	
}
