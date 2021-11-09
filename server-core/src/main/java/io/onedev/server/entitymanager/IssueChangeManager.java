package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeTitle(Issue issue, String title);
	
	void changeFields(Issue issue, Map<String, Object> fieldValues);

	void changeMilestones(Issue issue, Collection<Milestone> milestones);
	
	void addToMilestone(Issue issue, Milestone milestone);
	
	void removeFromMilestone(Issue issue, Milestone milestone);
	
	void changeState(Issue issue, String state, Map<String, Object> fieldValues, 
			Collection<String> removeFields, @Nullable String comment);
	
	void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, 
			@Nullable Collection<Milestone> milestone, Map<String, Object> fieldValues, 
			@Nullable String comment);
	
	List<IssueChange> queryAfter(Long afterChangeId, int count);
	
}
