package io.onedev.server.service.impl;

import javax.inject.Singleton;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestMention;
import io.onedev.server.model.User;
import io.onedev.server.service.PullRequestMentionService;

@Singleton
public class DefaultPullRequestMentionService extends BaseEntityService<PullRequestMention>
		implements PullRequestMentionService {

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
