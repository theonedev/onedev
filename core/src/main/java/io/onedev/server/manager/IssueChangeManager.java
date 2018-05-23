package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeTitle(Issue issue, String oldTitle);
	
	void changeDescription(Issue issue, String oldDescription);
	
	void changeFields(Issue issue, Serializable fieldBean, Map<String, IssueField> prevFields);
	
	void changeState(Issue issue, Serializable fieldBean, @Nullable String commentContent, 
			Map<String, IssueField> prevFields);
}
