package io.onedev.server.manager;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueManager extends EntityManager<Issue> {
	
	Class<? extends Serializable> defineCustomFieldsBeanClass(Project project);
	
	@Nullable
	Class<? extends Serializable> loadCustomFieldsBeanClass(String className);
	
	List<Issue> query(@Nullable Project project, User user);
	
}
