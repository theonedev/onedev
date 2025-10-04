package io.onedev.server.service;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestMention;
import io.onedev.server.model.User;

public interface PullRequestMentionService extends EntityService<PullRequestMention> {

	void mention(PullRequest request, User user);

}
