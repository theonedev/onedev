package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentRelation;
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
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(CodeCommentReply reply, CompareContext compareContext, PullRequest request) {
		boolean isNew = reply.isNew();
		dao.persist(reply);
		if (isNew) {
			CodeCommentReplied event = new CodeCommentReplied(reply, request); 
			CodeComment comment = reply.getComment();
			comment.setCompareContext(compareContext);
			comment.setReplyCount(comment.getReplyCount()+1);
			for (CodeCommentRelation relation: comment.getRelations()) 
				relation.getRequest().setCommentCount(relation.getRequest().getCommentCount()+1);
			listenerRegistry.post(event);
		}
	}

	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		super.delete(reply);
		CodeComment comment = reply.getComment();
		comment.setReplyCount(comment.getReplyCount()-1);
		for (CodeCommentRelation relation: comment.getRelations())
			relation.getRequest().setCommentCount(relation.getRequest().getCommentCount()-1);
	}
	
}
