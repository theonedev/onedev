package com.pmease.gitplex.core.entity.persistlistener;

import java.io.Serializable;
import java.util.Collection;

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
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.util.markdown.PullRequestParser;

@Singleton
public class PullRequestReferenceGenerator implements PersistListener {

	private final MarkdownManager markdownManager;
	
	private final Dao dao;
	
	private final AccountManager userManager;
	
	@Inject
	public PullRequestReferenceGenerator(MarkdownManager markdownManager, 
			Dao dao, AccountManager userManager, 
			UrlManager urlManager) {
		this.markdownManager = markdownManager;
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
					if (!Objects.equal(description, prevDescription) && description != null) {
						String html = markdownManager.parse(description);
						for (PullRequest referenced: new PullRequestParser().parseRequests(html))
							saveReference(referenced, request);
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) currentState[i];
					String prevContent = (String) previousState[i];
					if (!content.equals(prevContent)) {
						String html = markdownManager.parse(content);
						Collection<PullRequest> requests = new PullRequestParser().parseRequests(html);
						for (PullRequest request: requests)
							saveReference(request, comment.getRequest());
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestStatusChange) {
			PullRequestStatusChange statusChange = (PullRequestStatusChange) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("note")) {
					String note = (String) currentState[i];
					String prevNote = (String) previousState[i];
					if (!note.equals(prevNote)) {
						String html = markdownManager.parse(note);
						Collection<PullRequest> requests = new PullRequestParser().parseRequests(html);
						for (PullRequest request: requests)
							saveReference(request, statusChange.getRequest());
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
				if (propertyNames[i].equals("description")) {
					String description = (String) state[i];
					if (description != null) {
						String html = markdownManager.parse(description);
						for (PullRequest referenced: new PullRequestParser().parseRequests(html))
							saveReference(referenced, request);
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String html = markdownManager.parse(content);
					Collection<PullRequest> requests = new PullRequestParser().parseRequests(html);
					for (PullRequest request: requests)
						saveReference(request, comment.getRequest());
					break;
				}
			}
		} else if (entity instanceof PullRequestStatusChange) {
			PullRequestStatusChange statusChange = (PullRequestStatusChange) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("note")) {
					String note = (String) state[i];
					if (note != null) {
						String html = markdownManager.parse(note);
						Collection<PullRequest> requests = new PullRequestParser().parseRequests(html);
						for (PullRequest request: requests)
							saveReference(request, statusChange.getRequest());
					}
					break;
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
