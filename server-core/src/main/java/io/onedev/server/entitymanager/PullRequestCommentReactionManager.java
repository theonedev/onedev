package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestCommentReactionManager extends EntityManager<PullRequestCommentReaction> {

    void create(PullRequestCommentReaction reaction);

    void toggleEmoji(User user, PullRequestComment comment, String emoji);

} 