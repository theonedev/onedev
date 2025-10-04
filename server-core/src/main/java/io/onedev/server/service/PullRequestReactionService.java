package io.onedev.server.service;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReaction;
import io.onedev.server.model.User;

public interface PullRequestReactionService extends EntityService<PullRequestReaction> {

    void create(PullRequestReaction reaction);

    void toggleEmoji(User user, PullRequest request, String emoji);

} 