package com.gitplex.server.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReference;
import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.event.pullrequest.PullRequestChangeEvent;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.PullRequestReferenceManager;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.util.markdown.MarkdownManager;
import com.gitplex.server.util.markdown.PullRequestParser;

@Singleton
public class DefaultPullRequestReferenceManager extends AbstractEntityManager<PullRequestReference> 
		implements PullRequestReferenceManager {

	private final MarkdownManager markdownManager;
	
	private final AccountManager accountManager;
	
	@Inject
	public DefaultPullRequestReferenceManager(Dao dao, AccountManager accountManager, 
			MarkdownManager markdownManager) {
		super(dao);
		this.markdownManager = markdownManager;
		this.accountManager = accountManager;
	}

	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event instanceof MarkdownAware) {
			String markdown = ((MarkdownAware)event).getMarkdown();
			if (markdown != null) {
				String html = markdownManager.parse(markdown);
				
				for (PullRequest referenced: new PullRequestParser().parseRequests(html)) {
					if (!referenced.equals(event.getRequest())) {
						EntityCriteria<PullRequestReference> criteria = EntityCriteria.of(PullRequestReference.class);
						criteria.add(Restrictions.eq("referenced", referenced));
						criteria.add(Restrictions.eq("referencedBy", event.getRequest()));
						if (dao.find(criteria) == null) {
							PullRequestReference reference = new PullRequestReference();
							reference.setReferencedBy(event.getRequest());
							reference.setReferenced(referenced);
							reference.setDate(new Date());
							reference.setUser(accountManager.getCurrent());
							save(reference);
						}
					}					
				}
			}
		}
	}
	
}
