package io.onedev.server.service;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Stopwatch;
import io.onedev.server.model.User;

import org.jspecify.annotations.Nullable;

public interface StopwatchService extends EntityService<Stopwatch> {
	
	@Nullable
	Stopwatch find(User user, Issue issue);
	
	Stopwatch startWork(User user, Issue issue);
	
	void stopWork(Stopwatch stopwatch);
	
}
