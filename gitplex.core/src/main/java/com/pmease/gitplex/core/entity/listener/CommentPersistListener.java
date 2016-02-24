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
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.util.markdown.MentionParser;
import com.pmease.gitplex.core.util.markdown.PullRequestParser;

@Singleton
public class CommentPersistListener implements PersistListener {

	private final MarkdownManager markdownManager;

	private final Dao dao;
	
	private final UserManager userManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public CommentPersistListener(MarkdownManager markdownManager, Dao dao, 
			UserManager userManager, Set<PullRequestListener> pullRequestListeners) {
		this.markdownManager = markdownManager;
		this.dao = dao;
		this.userManager = userManager;
		this.pullRequestListeners = pullRequestListeners;
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		if (entity instanceof Comment) {
			Comment comment = (Comment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) currentState[i];
					String prevContent = (String) previousState[i];
					if (!content.equals(prevContent)) {
						String html = markdownManager.parse(content);
						String prevHtml = markdownManager.parse(prevContent);

						MentionParser mentionParser = new MentionParser();
						Collection<User> mentions = mentionParser.parseMentions(html);
						
						mentions.removeAll(mentionParser.parseMentions(prevHtml));
						for (User user: mentions) {
							for (PullRequestListener listener: pullRequestListeners)
								listener.onMentioned((Comment) entity, user);
						}
						
						Collection<PullRequest> requests = new PullRequestParser().parseRequests(html);
						for (PullRequest request: requests)
							saveReference(request, comment.getRequest());
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
		if (entity instanceof Comment) {
			Comment comment = (Comment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String html = markdownManager.parse(content);
					Collection<User> mentions = new MentionParser().parseMentions(html);
					for (User user: mentions) {
						for (PullRequestListener listener: pullRequestListeners)
							listener.onMentioned((Comment) entity, user);
					}
					
					Collection<PullRequest> requests = new PullRequestParser().parseRequests(html);
					for (PullRequest request: requests)
						saveReference(request, comment.getRequest());
					
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
