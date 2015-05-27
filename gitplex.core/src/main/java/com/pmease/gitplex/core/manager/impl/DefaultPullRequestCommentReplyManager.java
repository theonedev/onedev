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
import com.pmease.gitplex.core.comment.MentionParser;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentReplyManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultPullRequestCommentReplyManager implements PullRequestCommentReplyManager {

	private final Dao dao;

	private final MarkdownManager markdownManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultPullRequestCommentReplyManager(Dao dao, MarkdownManager markdownManager, 
			Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.markdownManager = markdownManager;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(final PullRequestCommentReply reply) {
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
	public Collection<PullRequestCommentReply> findBy(PullRequest request) {
		EntityCriteria<PullRequestCommentReply> criteria = EntityCriteria.of(PullRequestCommentReply.class);
		criteria.createCriteria("comment").add(Restrictions.eq("request", request));
		return dao.query(criteria);
	}

}
