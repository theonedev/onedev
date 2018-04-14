package io.onedev.server.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueSort;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.UndefinedStateResolution;

public interface IssueManager extends EntityManager<Issue> {
	
	void save(Issue issue, Serializable fieldBean, Collection<String> fieldNames);
	
	void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions);
	
	Collection<String> getUndefinedStates(Project project);
	
	void test();
	
	List<Issue> query(IssueQuery issueQuery, List<IssueSort> issueSorts, int firstResult, int maxResults);
	
	long count(IssueQuery issueQuery);
	
}
