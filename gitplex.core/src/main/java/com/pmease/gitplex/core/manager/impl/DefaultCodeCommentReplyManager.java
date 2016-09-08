package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;

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
	public void save(CodeCommentReply reply, PullRequest request, boolean callListeners) {
		boolean isNew = reply.isNew();
		dao.persist(reply);
		if (isNew && callListeners) {
			CodeCommentReplied event = new CodeCommentReplied(reply, request); 
			listenerRegistry.post(event);
			reply.getComment().setLastEvent(event);
			codeCommentManager.save(reply.getComment());
		}
	}
	
	@Transactional
	@Override
	public void save(CodeCommentReply reply) {
		save(reply, null, true);
	}
	
}
