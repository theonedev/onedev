package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
	public void save(CodeCommentReply reply) {
		if (reply.isNew()) {
			dao.persist(reply);
			
			CodeComment comment = reply.getComment();
			comment.setReplyCount(comment.getReplyCount()+1);
			
			listenerRegistry.post(new CodeCommentReplied(reply));

			PullRequest request = comment.getRequest();
			if (request != null) {
				request.setCommentCount(request.getCommentCount()+1);
				listenerRegistry.post(new PullRequestCodeCommentReplied(request, reply));
			}
		} else {
			dao.persist(reply);
		}
	}

	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		super.delete(reply);
		CodeComment comment = reply.getComment();
		comment.setReplyCount(comment.getReplyCount()-1);
		PullRequest request = comment.getRequest();
		if (request != null)
			request.setCommentCount(request.getCommentCount()-1);
	}
	
}
