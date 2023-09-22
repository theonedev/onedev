package io.onedev.server.timetracking;

import io.onedev.server.model.Issue;

import javax.annotation.Nullable;

public interface TimeTrackingManager {
	
	void syncTotalEstimatedTime(Issue issue, @Nullable String aggregationLink,
								TimeAggregationDirection direction);

	void syncTotalSpentTime(Issue issue, @Nullable String aggregationLink,
							TimeAggregationDirection direction);

	void syncOwnSpentTime(Issue issue);
	
	int aggregateSourceLinkEstimatedTime(Issue issue, String linkName);
	
	int aggregateTargetLinkEstimatedTime(Issue issue, String linkName);

	int aggregateSourceLinkSpentTime(Issue issue, String linkName);

	int aggregateTargetLinkSpentTime(Issue issue, String linkName);
	
}
