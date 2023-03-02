package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

    void createOrUpdate(PullRequestComment comment);

    void createOrUpdate(PullRequestComment comment, Collection<String> notifiedEmailAddresses);
	
}
