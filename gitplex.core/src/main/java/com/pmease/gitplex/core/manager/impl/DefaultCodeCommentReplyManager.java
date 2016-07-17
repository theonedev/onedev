package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.TransactionInterceptor;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;

@Singleton
public class DefaultCodeCommentReplyManager extends AbstractEntityManager<CodeCommentReply> 
		implements CodeCommentReplyManager {

	private final Provider<Set<CodeCommentListener>> listenersProvider;
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, Provider<Set<CodeCommentListener>> listenersProvider) {
		super(dao);
		this.listenersProvider = listenersProvider;
	}

	@Transactional
	@Override
	public void save(CodeCommentReply reply) {
		boolean isNew = reply.isNew();
		dao.persist(reply);
		if (isNew && TransactionInterceptor.isInitiating()) {
			for (CodeCommentListener listener: listenersProvider.get())
				listener.onReplyComment(reply);
		}
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
