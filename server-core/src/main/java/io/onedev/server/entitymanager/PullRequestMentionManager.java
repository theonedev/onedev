package io.onedev.server.entitymanager;

import io.onedev.server.model.*;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestMentionManager extends EntityManager<PullRequestMention> {

	void mention(PullRequest request, User user);
		
}
