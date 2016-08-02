package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;

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
	public void save(CodeCommentReply reply, boolean notifyListeners) {
		boolean isNew = reply.isNew();
		dao.persist(reply);
		if (isNew && notifyListeners) {
			listenerRegistry.notify(new CodeCommentReplied(reply));
		}
	}
	
	@Transactional
	@Override
	public void save(CodeCommentReply reply) {
		save(reply, true);
	}

	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		CodeComment comment = reply.getComment();
		List<CodeCommentReply> replies = new ArrayList<>(comment.getReplies());
		replies.remove(reply);
		Collections.sort(replies);
		if (replies.isEmpty()) {
			comment.setLastEventUser(null);
			comment.setLastEventDate(comment.getCreateDate());
		} else {
			CodeCommentReply lastReply = replies.get(replies.size()-1);
			comment.setLastEventUser(lastReply.getUser());
			comment.setLastEventDate(lastReply.getDate());
		}
		dao.remove(reply);
	}
	
}
