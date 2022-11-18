package io.onedev.server.infomanager;

import java.util.Map;

import io.onedev.server.model.Issue;

public interface IssueInfoManager {

	Map<Integer, String> getDailyStates(Issue issue, Integer fromDay, Integer toDay);
	
}
