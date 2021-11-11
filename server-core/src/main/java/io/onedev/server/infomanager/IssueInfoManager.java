package io.onedev.server.infomanager;

import java.util.Map;

public interface IssueInfoManager {

	Map<Integer, String> getDailyStates(Long issueId, Integer fromDay, Integer toDay);
	
}
