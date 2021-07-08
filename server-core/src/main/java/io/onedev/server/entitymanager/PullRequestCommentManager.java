package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

	void save(PullRequestComment comment, Collection<String> notifiedEmailAddresses);
	
}
