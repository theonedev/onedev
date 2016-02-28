package com.pmease.gitplex.core.entity.listener;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;
import org.hibernate.CallbackException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.PersistListener;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.util.markdown.MentionParser;
import com.pmease.gitplex.core.util.markdown.PullRequestParser;

@Singleton
public class PullRequestPersistListener implements PersistListener {

	private final MarkdownManager markdownManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final Dao dao;
	
	private final AccountManager userManager;
	
	@Inject
	public PullRequestPersistListener(MarkdownManager markdownManager, 
			Set<PullRequestListener> pullRequestListeners, Dao dao, AccountManager userManager) {
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
		if (entity instanceof PullRequest) {
			PullRequest request = (PullRequest) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("description")) {
					String description = (String) currentState[i];
					String prevDescription = (String) previousState[i];
					if (!Objects.equal(description, prevDescription)) {
						MentionParser mentionParser = new MentionParser();
						Collection<Account> mentions;
						String html;
						if (description != null) {
							html = markdownManager.parse(description);
							mentions = mentionParser.parseMentions(html);
						} else {
							mentions = new HashSet<>();
							html = null;
						}
						if (prevDescription != null)
							mentions.removeAll(mentionParser.parseMentions(markdownManager.parse(prevDescription)));
						for (Account user: mentions) {
							for (PullRequestListener listener: pullRequestListeners)
								listener.onMentioned((PullRequest) entity, user);
						}
						
						if (html != null) {
							for (PullRequest referenced: new PullRequestParser().parseRequests(html))
								saveReference(referenced, request);
						}
					}
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
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (entity instanceof PullRequest) {
			PullRequest request = (PullRequest) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("idStr")) {
					state[i] = id.toString();
				} else if (propertyNames[i].equals("description")) {
					String description = (String) state[i];
					if (description != null) {
						String html = markdownManager.parse(description);
						Collection<Account> mentions = new MentionParser().parseMentions(html);
						for (Account user: mentions) {
							for (PullRequestListener listener: pullRequestListeners)
								listener.onMentioned((PullRequest) entity, user);
						}
						
						for (PullRequest referenced: new PullRequestParser().parseRequests(html))
							saveReference(referenced, request);
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
