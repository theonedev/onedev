package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueFieldManager extends EntityManager<IssueField> {

	Class<? extends Serializable> defineFieldBeanClass(Project project);
	
	@Nullable
	Class<? extends Serializable> loadFieldBeanClass(String className);
	
	Serializable loadFields(Issue issue);
	
	void saveFields(Issue issue, Serializable fieldBean);
	
	Set<String> getExcludedFields(Project project, String state);
	
}
