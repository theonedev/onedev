package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.watch.WatchStatus;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IssueWatchManager extends EntityManager<IssueWatch> {
	
	@Nullable
	IssueWatch find(Issue issue, User user);

	void watch(Issue issue, User user, boolean watching);

    void create(IssueWatch watch);

	void update(IssueWatch watch);
	
	void setWatchStatus(User user, Collection<Issue> issues, WatchStatus watchStatus);
	
}
