package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeTitle(Issue issue, String title);
	
	void changeDescription(Issue issue, @Nullable String description);
	
	void changeMilestone(Issue issue, Milestone milestone);
	
	void changeFields(Issue issue, Serializable fieldBean, Collection<String> fieldNames);
	
	void changeState(Issue issue, String state, Serializable fieldBean, Collection<String> fieldNames, 
			@Nullable String comment);
	
	void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, 
			@Nullable Optional<Milestone> milestone, Serializable fieldBean, 
			Collection<String> fieldNames, @Nullable String comment);
	
}
