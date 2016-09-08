package com.pmease.gitplex.core.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.event.MarkdownAware;
import com.pmease.gitplex.core.event.pullrequest.PullRequestChangeEvent;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestReferenceManager;
import com.pmease.gitplex.core.util.markdown.PullRequestParser;

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
