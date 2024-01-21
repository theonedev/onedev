package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Stopwatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface StopwatchManager extends EntityManager<Stopwatch> {
	
	@Nullable
	Stopwatch find(User user, Issue issue);
	
	Stopwatch startWork(User user, Issue issue);
	
	void stopWork(Stopwatch stopwatch);
	
}
