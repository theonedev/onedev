package io.onedev.server.entitymanager;

import java.util.Date;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueStateHistory;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;

public interface IssueStateHistoryManager extends EntityManager<IssueStateHistory> {
	
	Map<Integer, Map<String, Integer>> queryDurationStats(
			ProjectScope projectScope, @Nullable Criteria<Issue> issueFilter,  
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);

	Map<Integer, Map<String, Integer>> queryFrequencyStats(
			ProjectScope projectScope, @Nullable Criteria<Issue> issueFilter, 
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);
	
	Map<Integer, Map<String, Integer>> queryTrendStats(
			ProjectScope projectScope, @Nullable Criteria<Issue> issueFilter, 
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);

}