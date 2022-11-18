package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Milestone;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueChangeManager extends EntityManager<IssueChange> {

	void changeLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);
	
	void addLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);
	
	void removeLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);
	
	void changeTitle(Issue issue, String title);
	
	void changeConfidential(Issue issue, boolean confidential);
	
	void changeFields(Issue issue, Map<String, Object> fieldValues);

	void changeMilestones(Issue issue, Collection<Milestone> milestones);
	
	void save(IssueChange change, @Nullable String note);
	
	void addSchedule(Issue issue, Milestone milestone);
	
	void removeSchedule(Issue issue, Milestone milestone);
	
	void changeState(Issue issue, String state, Map<String, Object> fieldValues, 
			Collection<String> removeFields, @Nullable String comment);
	
	void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, @Nullable Boolean confidential,
			@Nullable Collection<Milestone> milestone, Map<String, Object> fieldValues, @Nullable String comment);
	
	List<IssueChange> queryAfter(Long projectId, Long afterChangeId, int count);
	
}
