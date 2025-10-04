package io.onedev.server.service.impl;

import javax.inject.Singleton;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueMention;
import io.onedev.server.model.User;
import io.onedev.server.service.IssueMentionService;

@Singleton
public class DefaultIssueMentionService extends BaseEntityService<IssueMention>
		implements IssueMentionService {

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
