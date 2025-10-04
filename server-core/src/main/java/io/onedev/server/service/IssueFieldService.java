package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;

public interface IssueFieldService extends EntityService<IssueField> {
	
	void saveFields(Issue issue);
	
	void onRenameUser(String oldName, String newName);

    void create(IssueField entity);

    void onRenameGroup(String oldName, String newName);
			
	void populateFields(Collection<Issue> issues);
	
}
