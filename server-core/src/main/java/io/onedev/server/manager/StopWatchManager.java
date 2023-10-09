package io.onedev.server.manager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.StopWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface StopWatchManager extends EntityManager<StopWatch> {
	
	@Nullable
	StopWatch find(User user, Issue issue);
	
	StopWatch startWork(User user, Issue issue);
	
	void stopWork(StopWatch stopWatch);
	
}
