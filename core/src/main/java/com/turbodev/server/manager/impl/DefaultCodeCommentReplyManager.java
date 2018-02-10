package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.ListenerRegistry;
import com.turbodev.server.event.codecomment.CodeCommentReplied;
import com.turbodev.server.manager.CodeCommentManager;
import com.turbodev.server.manager.CodeCommentReplyManager;
import com.turbodev.server.model.CodeCommentReply;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.support.CompareContext;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;

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
