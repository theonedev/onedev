package io.onedev.server.service;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueMention;
import io.onedev.server.model.User;

public interface IssueMentionService extends EntityService<IssueMention> {

	void mention(Issue issue, User user);

}
