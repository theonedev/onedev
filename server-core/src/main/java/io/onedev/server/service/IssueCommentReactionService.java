package io.onedev.server.service;

import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueCommentReaction;
import io.onedev.server.model.User;

public interface IssueCommentReactionService extends EntityService<IssueCommentReaction> {

    void create(IssueCommentReaction reaction);

    void toggleEmoji(User user, IssueComment comment, String emoji);

} 