package io.onedev.server.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.entityquery.EntityQuery;
import io.onedev.server.entityquery.issue.IssueCriteria;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValueResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedStateResolution;

public interface IssueManager extends EntityManager<Issue> {
	
    @Nullable
    Issue find(Project project, long number);
    
	void open(Issue issue);
	
	List<Issue> query(Project project, EntityQuery<Issue> issueQuery, int firstResult, int maxResults);
	
	int count(Project project, @Nullable IssueCriteria issueCriteria);
	
	List<Issue> query(Project project, @Nullable String term, int count);

	int count(Milestone milestone, @Nullable StateSpec.Category category);
	
	Collection<String> getUndefinedStates(Project project);
	
	void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions);
	
	Collection<String> getUndefinedFields(Project project);
	
	void fixUndefinedFields(Project project, Map<String, UndefinedFieldResolution> resolutions);
	
	Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project);
	
	void fixUndefinedFieldValues(Project project, Map<UndefinedFieldValue, UndefinedFieldValueResolution> resolutions);
	
	void fixFieldValueOrders(Project project);
	
}
