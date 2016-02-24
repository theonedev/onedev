package com.pmease.gitplex.core.entity.listener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.CallbackException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.PersistListener;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.util.markdown.MentionParser;
import com.pmease.gitplex.core.util.markdown.PullRequestParser;

@Singleton
public class CommentReplyPersistListener implements PersistListener {

	private final MarkdownManager markdownManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final Dao dao;
	
	private final UserManager userManager;
	
	@Inject
	public CommentReplyPersistListener(MarkdownManager markdownManager, 
			Set<PullRequestListener> pullRequestListeners, Dao dao, UserManager userManager) {
		this.markdownManager = markdownManager;
		this.pullRequestListeners = pullRequestListeners;
		this.dao = dao;
		this.userManager = userManager;
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		if (entity instanceof CommentReply) {
			CommentReply reply = (CommentReply) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) currentState[i];
					String prevContent = (String) previousState[i];
					if (!content.equals(prevContent)) {
						MentionParser parser = new MentionParser();
						String html = markdownManager.parse(content);
						Collection<User> mentions = parser.parseMentions(html);
						mentions.removeAll(parser.parseMentions(markdownManager.parse(prevContent)));
						for (User user: mentions) {
							for (PullRequestListener listener: pullRequestListeners)
								listener.onMentioned((CommentReply) entity, user);
						}
						
						for (PullRequest request: new PullRequestParser().parseRequests(html))
							saveReference(request, reply.getComment().getRequest());
					}
					break;
				}
			}
		} 
		return false;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (entity instanceof CommentReply) {
			CommentReply reply = (CommentReply) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String html = markdownManager.parse(content);
					Collection<User> mentions = new MentionParser().parseMentions(html);
					for (User user: mentions) {
						for (PullRequestListener listener: pullRequestListeners)
							listener.onMentioned((CommentReply) entity, user);
					}

					for (PullRequest request: new PullRequestParser().parseRequests(html))
						saveReference(request, reply.getComment().getRequest());
					break;
				}
			}
		} 
		return false;
	}

	private void saveReference(PullRequest referenced, PullRequest referencedBy) {
		if (!referenced.equals(referencedBy)) {
			EntityCriteria<PullRequestReference> criteria = EntityCriteria.of(PullRequestReference.class);
			criteria.add(Restrictions.eq("referenced", referenced));
			criteria.add(Restrictions.eq("referencedBy", referencedBy));
			if (dao.find(criteria) == null) {
				PullRequestReference reference = new PullRequestReference();
				reference.setReferencedBy(referencedBy);
				reference.setReferenced(referenced);
				reference.setUser(userManager.getCurrent());
				dao.persist(reference);
			}
		}
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}

}
