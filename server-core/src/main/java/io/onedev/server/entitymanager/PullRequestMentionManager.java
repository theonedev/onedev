package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestMention;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestMentionManager extends EntityManager<PullRequestMention> {

	void mention(PullRequest request, User user);

}
