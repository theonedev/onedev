package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueStateHistory;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IssueStateHistoryManager extends EntityManager<IssueStateHistory> {
	
	Map<Integer, Map<String, Integer>> queryDurationStats(
			ProjectScope projectScope, @Nullable Criteria<Issue> criteria, StatsGroup group);

	Map<Integer, Map<String, Integer>> queryFrequencyStats(
			ProjectScope projectScope, @Nullable Criteria<Issue> criteria, StatsGroup group);
	
}