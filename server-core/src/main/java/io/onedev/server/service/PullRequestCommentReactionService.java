package io.onedev.server.service;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentReaction;
import io.onedev.server.model.User;

public interface PullRequestCommentReactionService extends EntityService<PullRequestCommentReaction> {

    void create(PullRequestCommentReaction reaction);

    void toggleEmoji(User user, PullRequestComment comment, String emoji);

} 