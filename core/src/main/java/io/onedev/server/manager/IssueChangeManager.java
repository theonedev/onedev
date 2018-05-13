package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.PromptedField;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeTitle(Issue issue, String oldTitle);
	
	void changeDescription(Issue issue, String oldDescription);
	
	void changeFields(Issue issue, Serializable fieldBean, Map<String, PromptedField> prevFields, Collection<String> promptedFields);
	
}
