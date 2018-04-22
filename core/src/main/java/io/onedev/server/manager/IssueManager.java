package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.UndefinedStateResolution;

public interface IssueManager extends EntityManager<Issue> {
	
	void save(Issue issue, Serializable fieldBean, Collection<String> fieldNames);
	
	void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions);
	
	Collection<String> getUndefinedStates(Project project);
	
	void test();
	
	List<Issue> query(IssueQuery issueQuery, int firstResult, int maxResults);
	
	long count(@Nullable IssueCriteria issueCriteria);
	
}
