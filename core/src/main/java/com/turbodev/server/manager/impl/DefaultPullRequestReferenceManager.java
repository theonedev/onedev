package com.turbodev.server.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.server.event.MarkdownAware;
import com.turbodev.server.event.pullrequest.PullRequestEvent;
import com.turbodev.server.manager.MarkdownManager;
import com.turbodev.server.manager.PullRequestReferenceManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.PullRequestReference;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.util.markdown.PullRequestParser;

@Singleton
public class DefaultPullRequestReferenceManager extends AbstractEntityManager<PullRequestReference> 
		implements PullRequestReferenceManager {

	private final MarkdownManager markdownManager;
	
	private final UserManager userManager;
	
	@Inject
	public DefaultPullRequestReferenceManager(Dao dao, UserManager userManager, 
			MarkdownManager markdownManager) {
		super(dao);
		this.markdownManager = markdownManager;
		this.userManager = userManager;
	}

	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (event instanceof MarkdownAware) {
			String markdown = ((MarkdownAware)event).getMarkdown();
			if (markdown != null) {
				String rendered = markdownManager.render(markdown);
				
				for (PullRequest referenced: new PullRequestParser().parseRequests(rendered)) {
					if (!referenced.equals(event.getRequest())) {
						EntityCriteria<PullRequestReference> criteria = EntityCriteria.of(PullRequestReference.class);
						criteria.add(Restrictions.eq("referenced", referenced));
						criteria.add(Restrictions.eq("referencedBy", event.getRequest()));
						if (dao.find(criteria) == null) {
							PullRequestReference reference = new PullRequestReference();
							reference.setReferencedBy(event.getRequest());
							reference.setReferenced(referenced);
							reference.setDate(new Date());
							reference.setUser(userManager.getCurrent());
							save(reference);
						}
					}					
				}
			}
		}
	}
	
}
