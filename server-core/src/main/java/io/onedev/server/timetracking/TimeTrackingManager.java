package io.onedev.server.timetracking;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.layout.SidebarMenuItem;

import java.util.Collection;

public interface TimeTrackingManager {
	
	void requestToSyncTimes(Collection<Long> issueIds);
	
	int aggregateSourceLinkEstimatedTime(Issue issue, String linkName);
	
	int aggregateTargetLinkEstimatedTime(Issue issue, String linkName);

	int aggregateSourceLinkSpentTime(Issue issue, String linkName);

	int aggregateTargetLinkSpentTime(Issue issue, String linkName);

	SidebarMenuItem.Page newTimesheetsMenuItem(Project project);
	
}
