package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestReactionManager extends EntityManager<PullRequestReaction> {

    void create(PullRequestReaction reaction);

    void toggleEmoji(User user, PullRequest request, String emoji);

} 