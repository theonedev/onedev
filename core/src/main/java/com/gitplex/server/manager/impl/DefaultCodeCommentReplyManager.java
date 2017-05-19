package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentReplied;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentReplyManager;
import com.gitplex.server.model.CodeCommentReply;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

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
			PullRequestCodeCommentReplied event = new PullRequestCodeCommentReplied(reply); 
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
