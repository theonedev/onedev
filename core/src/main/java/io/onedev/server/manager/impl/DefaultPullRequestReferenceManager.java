package io.onedev.server.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.PullRequestReferenceManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReference;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.markdown.PullRequestParser;

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
