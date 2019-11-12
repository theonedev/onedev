package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public interface IssueManager extends EntityManager<Issue> {
	
    @Nullable
    Issue find(Project project, long number);
    
	void open(Issue issue);
	
	List<Issue> query(@Nullable Project project, @Nullable User user, EntityQuery<Issue> issueQuery, int firstResult, int maxResults);
	
	int count(@Nullable Project project, @Nullable User user, @Nullable IssueCriteria issueCriteria);
	
	List<Issue> query(Project project, @Nullable String term, int count);

	int count(Milestone milestone, @Nullable User user, @Nullable StateSpec.Category category);
	
	Collection<String> getUndefinedStates();
	
	void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions);
	
	Collection<String> getUndefinedFields();
	
	void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions);
	
	Collection<UndefinedFieldValue> getUndefinedFieldValues();
	
	void fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits);
	
	void fixFieldValueOrders();
	
	void delete(Issue issue);
	
	Collection<Long> getIssueNumbers(Long projectId);
	
}
