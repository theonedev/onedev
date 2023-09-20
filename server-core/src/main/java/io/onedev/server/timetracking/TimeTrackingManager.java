package io.onedev.server.timetracking;

import io.onedev.server.model.Issue;

import javax.annotation.Nullable;

public interface TimeTrackingManager {
	
	void syncEstimatedTime(Issue issue, String estimatedTimeField, @Nullable String aggregationLink,  
						   TimeAggregationDirection direction);

	void syncSpentTime(Issue issue, String spentTimeField, @Nullable String aggregationLink, 
						   TimeAggregationDirection direction);

	int aggregateSourceLinkTimes(Issue issue, String fieldName, String linkName);
	
	int aggregateTargetLinkTimes(Issue issue, String fieldName, String linkName);
	
}
