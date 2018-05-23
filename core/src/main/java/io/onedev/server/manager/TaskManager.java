package io.onedev.server.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Task;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface TaskManager extends EntityManager<Task> {

	Collection<Task> findAll(PullRequest request, @Nullable User user, @Nullable String description);
	
	Collection<Task> findAll(Issue issue, @Nullable User user, @Nullable String description);
	
	void deleteTasks(PullRequest request, @Nullable User user, @Nullable String description);
	
	Task addTask(PullRequest request, User user, String description);
	
	Task addTask(Issue issue, User user, String description);
	
	void deleteTasks(Issue issue, @Nullable User user, @Nullable String description);
}
