package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueMention;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueMentionManager extends EntityManager<IssueMention> {

	void mention(Issue issue, User user);

}
