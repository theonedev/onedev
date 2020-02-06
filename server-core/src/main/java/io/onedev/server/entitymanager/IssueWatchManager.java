package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueWatchManager extends EntityManager<IssueWatch> {
	
	@Nullable
	IssueWatch find(Issue issue, User user);

	void watch(Issue issue, User user, boolean watching);

}
