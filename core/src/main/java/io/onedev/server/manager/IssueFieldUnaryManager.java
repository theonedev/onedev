package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueFieldUnaryManager extends EntityManager<IssueFieldUnary> {

	Class<? extends Serializable> defineFieldBeanClass(Project project);
	
	@Nullable
	Class<? extends Serializable> loadFieldBeanClass(String className);
	
	Serializable readFields(Issue issue);
	
	void writeFields(Issue issue, Serializable fieldBean, Collection<String> promptedFields);
	
	Set<String> getExcludedFields(Issue issue, String state);
	
	void onRenameUser(String oldName, String newName);
	
	void onRenameGroup(String oldName, String newName);
			
	void populateFields(List<Issue> issues);
	
}
