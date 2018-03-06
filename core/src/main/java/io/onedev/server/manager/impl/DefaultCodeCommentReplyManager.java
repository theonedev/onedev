package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.CodeCommentReplyManager;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
