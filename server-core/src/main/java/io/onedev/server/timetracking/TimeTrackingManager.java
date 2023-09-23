package io.onedev.server.timetracking;

import io.onedev.server.model.Issue;

import javax.annotation.Nullable;

public interface TimeTrackingManager {
	
	void syncTimes(Issue issue);
	
	void syncTotalEstimatedTime(Issue issue, @Nullable LinkAggregation linkAggregation);

	void syncTotalSpentTime(Issue issue, @Nullable LinkAggregation linkAggregation);

	void syncOwnSpentTime(Issue issue);
	
	int aggregateSourceLinkEstimatedTime(Issue issue, String linkName);
	
	int aggregateTargetLinkEstimatedTime(Issue issue, String linkName);

	int aggregateSourceLinkSpentTime(Issue issue, String linkName);

	int aggregateTargetLinkSpentTime(Issue issue, String linkName);
	
}
