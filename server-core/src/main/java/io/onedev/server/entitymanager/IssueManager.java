package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.util.MilestoneAndState;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public interface IssueManager extends EntityManager<Issue> {
	
    @Nullable
    Issue find(Project project, long number);
    
    @Nullable
    Issue find(ProjectScopedNumber issueFQN);
    
    @Nullable
    Issue find(String issueFQN);
    
	void open(Issue issue);
	
	List<Issue> query(@Nullable Project project, EntityQuery<Issue> issueQuery, 
			int firstResult, int maxResults, boolean loadFields);
	
	int count(@Nullable Project project, @Nullable IssueCriteria issueCriteria);
	
	List<Issue> query(Project project, String term, int count);

	Collection<String> getUndefinedStates();
	
	void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions);
	
	Collection<String> getUndefinedFields();
	
	void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions);
	
	Collection<UndefinedFieldValue> getUndefinedFieldValues();
	
	void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions);
	
	void fixFieldValueOrders();
	
	void delete(Issue issue);
	
	Collection<Long> getIssueNumbers(Long projectId);
	
	Collection<MilestoneAndState> queryMilestoneAndStates(Project project, Collection<Milestone> milestones);
}
