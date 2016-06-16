package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.listener.CodeCommentReplyListener;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;

@Singleton
public class DefaultCodeCommentReplyManager extends AbstractEntityDao<CodeCommentReply> 
		implements CodeCommentReplyManager {

	private final CodeCommentManager codeCommentManager;
	
	private final Provider<Set<CodeCommentReplyListener>> listenersProvider;
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, CodeCommentManager codeCommentManager, 
			Provider<Set<CodeCommentReplyListener>> listenersProvider) {
		super(dao);
		this.codeCommentManager = codeCommentManager;
		this.listenersProvider = listenersProvider;
	}

	@Transactional
	@Override
	public void save(CodeCommentReply reply) {
		for (CodeCommentReplyListener listener: listenersProvider.get())
			listener.onSaveReply(reply);
		persist(reply);
		reply.getComment().setUpdateDate(reply.getDate());
		reply.getComment().setLastReplyUser(reply.getUser().getDisplayName());
		codeCommentManager.save(reply.getComment());
	}

	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		for (CodeCommentReplyListener listener: listenersProvider.get())
			listener.onDeleteReply(reply);
		CodeComment comment = reply.getComment();
		List<CodeCommentReply> replies = new ArrayList<>(comment.getReplies());
		replies.remove(reply);
		Collections.sort(replies);
		if (replies.isEmpty()) {
			comment.setLastReplyUser(null);
			comment.setUpdateDate(comment.getCreateDate());
		} else {
			CodeCommentReply lastReply = replies.get(replies.size()-1);
			comment.setLastReplyUser(lastReply.getUser().getDisplayName());
			comment.setUpdateDate(lastReply.getDate());
		}
		remove(reply);
	}
}
