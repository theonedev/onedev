package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.codecomment.CodeCommentReplyCreated;
import io.onedev.server.event.project.codecomment.CodeCommentReplyDeleted;
import io.onedev.server.event.project.codecomment.CodeCommentReplyEdited;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentReplyCreated;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultCodeCommentReplyManager extends BaseEntityManager<CodeCommentReply> 
		implements CodeCommentReplyManager {
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void create(CodeCommentReply reply) {
		Preconditions.checkState(reply.isNew());
		dao.persist(reply);
		
		CodeComment comment = reply.getComment();
		comment.setReplyCount(comment.getReplyCount()+1);
		
		listenerRegistry.post(new CodeCommentReplyCreated(reply));

		PullRequest request = comment.getCompareContext().getPullRequest();
		if (request != null) 
			listenerRegistry.post(new PullRequestCodeCommentReplyCreated(request, reply));
	}

	@Transactional
	@Override
	public void update(CodeCommentReply reply) {
 		Preconditions.checkState(!reply.isNew());
		dao.persist(reply);
		listenerRegistry.post(new CodeCommentReplyEdited(reply));
	}
	
	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		super.delete(reply);
		CodeComment comment = reply.getComment();
		comment.setReplyCount(comment.getReplyCount()-1);
		listenerRegistry.post(new CodeCommentReplyDeleted(reply));
	}
	
}
