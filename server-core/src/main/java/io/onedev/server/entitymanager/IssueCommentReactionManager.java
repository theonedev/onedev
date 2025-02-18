package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueCommentReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueCommentReactionManager extends EntityManager<IssueCommentReaction> {

    void create(IssueCommentReaction reaction);

    void toggleEmoji(User user, IssueComment comment, String emoji);

} 