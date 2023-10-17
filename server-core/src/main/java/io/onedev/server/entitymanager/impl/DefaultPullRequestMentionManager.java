package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.PullRequestMentionManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestMention;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultPullRequestMentionManager extends BaseEntityManager<PullRequestMention>
		implements PullRequestMentionManager {

	@Inject
	public DefaultPullRequestMentionManager(Dao dao) {
		super(dao);
	}

	@Override
	public void mention(PullRequest request, User user) {
		if (request.getMentions().stream().noneMatch(it->it.getUser().equals(user))) {
			PullRequestMention mention = new PullRequestMention();
			mention.setRequest(request);
			mention.setUser(user);
			dao.persist(mention);
		}
	}

}
