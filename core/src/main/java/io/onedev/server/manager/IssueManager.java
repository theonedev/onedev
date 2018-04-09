package io.onedev.server.manager;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueManager extends EntityManager<Issue> {
	
	List<Issue> query(@Nullable Project project, User user);
	
	void save(Issue issue, Serializable fieldBean);
	
	void renameState(Project project, String oldState, String newState);
	
}
