package io.onedev.server.entitymanager;

import java.util.List;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldEntity;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueFieldEntityManager extends EntityManager<IssueFieldEntity> {
	
	void saveFields(Issue issue);
	
	void onRenameUser(String oldName, String newName);
	
	void onRenameGroup(String oldName, String newName);
			
	void populateFields(List<Issue> issues);
	
}
