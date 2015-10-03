package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.CallbackException;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.HibernateListener;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.markdown.MentionParser;
import com.pmease.gitplex.core.markdown.PullRequestParser;

@Singleton
public class CommentPersistListener implements HibernateListener {

	private final MarkdownManager markdownManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public CommentPersistListener(MarkdownManager markdownManager, 
			Set<PullRequestListener> pullRequestListeners) {
		this.markdownManager = markdownManager;
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
						
						PullRequestParser requestParser = new PullRequestParser();
						Collection<PullRequest> requests = requestParser.parseRequests(html);
						
						requests.removeAll(requestParser.parseRequests(prevHtml));
						for (PullRequest request: requests) {
						}
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
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String rawHtml = markdownManager.parse(content);
					Collection<User> mentions = new MentionParser().parseMentions(rawHtml);
					for (User user: mentions) {
						for (PullRequestListener listener: pullRequestListeners)
							listener.onMentioned((Comment) entity, user);
					}
					break;
				}
			}
		} 
		return false;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}

}
