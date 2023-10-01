package io.onedev.server.infomanager;

import java.util.Map;

import io.onedev.server.model.Issue;

public interface IssueInfoManager {

	Map<Long, String> getDailyStates(Issue issue, Long fromDay, Long toDay);
	
	Map<Long, Integer> getDailySpentTimes(Issue issue, Long fromDay, Long toDay);
	
}
