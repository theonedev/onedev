package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Milestone;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface IssueChangeManager extends EntityManager<IssueChange> {
	
	void addLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);
	
	void removeLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);

	void changeLink(LinkSpec spec, Issue issue, @Nullable Issue prevLinkedIssue, @Nullable Issue linkedIssue, boolean opposite);
	
	void changeTitle(Issue issue, String title);
	
	void changeOwnEstimatedTime(Issue issue, int ownEstimatedTime);
	
	void changeOwnSpentTime(Issue issue, int ownSpentTime);
	
	void changeTotalEstimatedTime(Issue issue, int totalEstimatedTime);

	void changeTotalSpentTime(Issue issue, int totalSpentTime);

	void changeDescription(Issue issue, String description);
	
	void changeConfidential(Issue issue, boolean confidential);
	
	void changeFields(Issue issue, Map<String, Object> fieldValues);

	void changeMilestones(Issue issue, Collection<Milestone> milestones);
	
	void create(IssueChange change, @Nullable String note);
	
	void addSchedule(Issue issue, Milestone milestone);
	
	void removeSchedule(Issue issue, Milestone milestone);
	
	void changeState(Issue issue, String state, Map<String, Object> fieldValues, 
			Collection<String> removeFields, @Nullable String comment);
	
	void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, @Nullable Boolean confidential,
			@Nullable Collection<Milestone> milestone, Map<String, Object> fieldValues, @Nullable String comment);
	
	List<IssueChange> queryAfter(Long projectId, Long afterChangeId, int count);

}
