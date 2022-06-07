package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.event.codecomment.CodeCommentStatusChanged;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultCodeCommentStatusChangeManager extends BaseEntityManager<CodeCommentStatusChange>
		implements CodeCommentStatusChangeManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultCodeCommentStatusChangeManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Override
	public void save(CodeCommentStatusChange change) {
		save(change, null);
	}

	@Transactional
	@Override
	public void save(CodeCommentStatusChange change, String note) {
		change.getComment().setResolved(change.isResolved());
		dao.persist(change);
		
		if (note != null) {
			CodeCommentReply reply = new CodeCommentReply();
			reply.setComment(change.getComment());
			reply.setCompareContext(change.getCompareContext());
			reply.setContent(note);
			reply.setDate(change.getDate());
			reply.setUser(change.getUser());
			dao.persist(reply);
		}
		listenerRegistry.post(new CodeCommentStatusChanged(change, note));
	}
	
}
