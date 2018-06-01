package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeTitle(Issue issue, String prevTitle);
	
	void changeDescription(Issue issue, String prevDescription);
	
	void changeMilestone(Issue issue, String prevMilestone);
	
	void changeFields(Issue issue, Serializable fieldBean, Map<String, IssueField> prevFields, 
			Collection<String> promptedFields);
	
	void changeState(Issue issue, Serializable fieldBean, @Nullable String commentContent, 
			String prevState, Map<String, IssueField> prevFields, Collection<String> promptedFields);
}
