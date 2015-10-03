package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;
import org.hibernate.CallbackException;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.HibernateListener;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.markdown.MentionParser;

@Singleton
public class PullRequestPersistListener implements HibernateListener {

	private final MarkdownManager markdownManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public PullRequestPersistListener(MarkdownManager markdownManager, 
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
		if (entity instanceof PullRequest) {
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("description")) {
					String description = (String) currentState[i];
					String prevDescription = (String) previousState[i];
					if (!Objects.equal(description, prevDescription)) {
						MentionParser parser = new MentionParser();
						Collection<User> mentions;
						if (description != null)
							mentions = parser.parseMentions(markdownManager.parse(description));
						else
							mentions = new HashSet<>();
						if (prevDescription != null)
							mentions.removeAll(parser.parseMentions(markdownManager.parse(prevDescription)));
						for (User user: mentions) {
							for (PullRequestListener listener: pullRequestListeners)
								listener.onMentioned((PullRequest) entity, user);
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
		if (entity instanceof PullRequest) {
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("idStr")) {
					state[i] = id.toString();
				} else if (propertyNames[i].equals("description")) {
					String description = (String) state[i];
					if (description != null) {
						String rawHtml = markdownManager.parse(description);
						Collection<User> mentions = new MentionParser().parseMentions(rawHtml);
						for (User user: mentions) {
							for (PullRequestListener listener: pullRequestListeners)
								listener.onMentioned((PullRequest) entity, user);
						}
					}
				}
			}
		} 
		return true;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}

}
