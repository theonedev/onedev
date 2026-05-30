package io.onedev.server.service;

import io.onedev.server.model.IssueWork;
import io.onedev.server.model.IssueWorkReaction;
import io.onedev.server.model.User;

public interface IssueWorkReactionService extends EntityService<IssueWorkReaction> {

	void create(IssueWorkReaction reaction);

	void toggleEmoji(User user, IssueWork work, String emoji);

}
