package io.onedev.server.manager;

import java.util.List;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueFieldUnaryManager extends EntityManager<IssueFieldUnary> {
	
	void saveFields(Issue issue);
	
	void onRenameUser(String oldName, String newName);
	
	void onRenameGroup(String oldName, String newName);
			
	void populateFields(List<Issue> issues);
	
}
