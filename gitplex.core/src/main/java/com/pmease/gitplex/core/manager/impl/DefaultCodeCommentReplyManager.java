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
import com.pmease.gitplex.core.listener.CodeCommentReplyListener;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.security.SecurityUtils;

@Singleton
public class DefaultCodeCommentReplyManager extends AbstractEntityManager<CodeCommentReply> 
		implements CodeCommentReplyManager {

	private final VisitInfoManager visitInfoManager;
	
	private final Provider<Set<CodeCommentReplyListener>> listenersProvider;
	
	@Inject
	public DefaultCodeCommentReplyManager(Dao dao, 
			VisitInfoManager visitInfoManager, Provider<Set<CodeCommentReplyListener>> listenersProvider) {
		super(dao);
		this.visitInfoManager = visitInfoManager;
		this.listenersProvider = listenersProvider;
	}

	@Transactional
	@Override
	public void save(CodeCommentReply reply) {
		if (TransactionInterceptor.isInitiating()) {
			for (CodeCommentReplyListener listener: listenersProvider.get())
				listener.onSaveReply(reply);
		}
		dao.persist(reply);
		if (reply.getDate().after(reply.getComment().getUpdateDate())) {
			reply.getComment().setUpdateDate(reply.getDate());
			reply.getComment().setLastReplyUser(reply.getUser().getDisplayName());
			dao.persist(reply.getComment());
		}
		visitInfoManager.visit(SecurityUtils.getAccount(), reply.getComment());
	}

	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		if (TransactionInterceptor.isInitiating()) {
			for (CodeCommentReplyListener listener: listenersProvider.get())
				listener.onDeleteReply(reply);
		}
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
		dao.remove(reply);
	}
}
