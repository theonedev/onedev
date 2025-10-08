package io.onedev.server.service;

import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.shiro.subject.Subject;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;

public interface IssueWorkService extends EntityService<IssueWork> {
	
	void createOrUpdate(IssueWork work);
	
	List<IssueWork> query(User user, Issue issue, Date fromDate, Date toDate);
	
	List<IssueWork> query(Subject subject, @Nullable ProjectScope projectScope, EntityQuery<Issue> issueQuery, Date fromDate, Date toDate);
	
}
