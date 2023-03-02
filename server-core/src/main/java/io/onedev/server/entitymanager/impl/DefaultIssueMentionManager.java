package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.IssueMentionManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueMention;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultIssueMentionManager extends BaseEntityManager<IssueMention>
		implements IssueMentionManager {

	@Inject
	public DefaultIssueMentionManager(Dao dao) {
		super(dao);
	}

	@Override
	public void mention(Issue issue, User user) {
		if (issue.getMentions().stream().noneMatch(it->it.getUser().equals(user))) {
			IssueMention mention = new IssueMention();
			mention.setIssue(issue);
			mention.setUser(user);
			dao.persist(mention);
		}
	}

}
