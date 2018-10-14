package io.onedev.server.manager;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeTitle(Issue issue, String title, @Nullable User user);
	
	void changeDescription(Issue issue, @Nullable String description, @Nullable User user);
	
	void changeMilestone(Issue issue, Milestone milestone, @Nullable User user);
	
	void changeFields(Issue issue, Map<String, Object> fieldValues, @Nullable User user);
	
	void changeState(Issue issue, String state, Map<String, Object> fieldValues, @Nullable String comment, @Nullable User user);
	
	void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, 
			@Nullable Optional<Milestone> milestone, Map<String, Object> fieldValues, 
			@Nullable String comment, @Nullable User user);
	
}
