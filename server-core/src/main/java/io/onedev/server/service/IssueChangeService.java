package io.onedev.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.User;

public interface IssueChangeService extends EntityService<IssueChange> {
	
	void addLink(User user, LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);
	
	void removeLink(User user, LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite);

	void changeLink(User user, LinkSpec spec, Issue issue, @Nullable Issue prevLinkedIssue, @Nullable Issue linkedIssue, boolean opposite);
	
	void changeTitle(User user, Issue issue, String title);
	
	void changeOwnEstimatedTime(User user, Issue issue, int ownEstimatedTime);
	
	void changeOwnSpentTime(User user, Issue issue, int ownSpentTime);
	
	void changeTotalEstimatedTime(User user, Issue issue, int totalEstimatedTime);

	void changeTotalSpentTime(User user, Issue issue, int totalSpentTime);

	void changeDescription(User user, Issue issue, String description);
	
	void changeConfidential(User user, Issue issue, boolean confidential);
	
	void changeFields(User user, Issue issue, Map<String, Object> fieldValues);

	void changeIterations(User user, Issue issue, Collection<Iteration> iterations);
	
	void create(IssueChange change, @Nullable String note);
	
	void addSchedule(User user, Issue issue, Iteration iteration);

	void changeSchedule(User user, List<Issue> issues, @Nullable Iteration addIteration, 
						@Nullable Iteration removeIteration, boolean sendNotifications);
	
	void removeSchedule(User user, Issue issue, Iteration iteration);
	
	void changeState(User user, Issue issue, String state, Map<String, Object> fieldValues, 
			Collection<String> promptFields, Collection<String> removeFields, @Nullable String comment);
	
	void batchUpdate(User user, Iterator<? extends Issue> issues, @Nullable String state, @Nullable Boolean confidential,
                     @Nullable Collection<Iteration> iterations, Map<String, Object> fieldValues, 
					 @Nullable String comment, boolean sendNotifications);
	
	List<IssueChange> queryAfter(Long projectId, Long afterChangeId, int count);

	List<IssueChange> query(User submitter, Date fromDate, Date toDate);
}
