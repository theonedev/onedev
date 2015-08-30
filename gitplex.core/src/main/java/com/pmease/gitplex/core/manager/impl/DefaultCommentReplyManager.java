package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.MentionParser;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentReplyManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.CommentReply;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultCommentReplyManager implements CommentReplyManager {

	private final Dao dao;

	private final MarkdownManager markdownManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultCommentReplyManager(Dao dao, MarkdownManager markdownManager, 
			Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.markdownManager = markdownManager;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(final CommentReply reply) {
		boolean isNew = reply.isNew();
		
		dao.persist(reply);

		if (isNew) {
			String rawHtml = markdownManager.parse(reply.getContent());
			Collection<User> mentions = new MentionParser().parseMentions(rawHtml);
			for (User user: mentions) {
				for (PullRequestListener listener: pullRequestListeners)
					listener.onMentioned(reply, user);
			}
		}
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onCommentReplied(reply);
	}

	@Sessional
	@Override
	public Collection<CommentReply> findBy(PullRequest request) {
		EntityCriteria<CommentReply> criteria = EntityCriteria.of(CommentReply.class);
		criteria.createCriteria("comment").add(Restrictions.eq("request", request));
		return dao.query(criteria);
	}

}
