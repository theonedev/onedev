package io.onedev.server.service;

import java.util.Date;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.jetbrains.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueStateHistory;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;

public interface IssueStateHistoryService extends EntityService<IssueStateHistory> {
	
	Map<Integer, Map<String, Integer>> queryDurationStats(
			Subject subject, ProjectScope projectScope, @Nullable Criteria<Issue> issueFilter,  
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);

	Map<Integer, Map<String, Integer>> queryFrequencyStats(
			Subject subject, ProjectScope projectScope, @Nullable Criteria<Issue> issueFilter, 
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);
	
	Map<Integer, Map<String, Integer>> queryTrendStats(
			Subject subject, ProjectScope projectScope, @Nullable Criteria<Issue> issueFilter, 
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);

}