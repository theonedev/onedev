package io.onedev.server.entitymanager;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;

public interface IssueWorkManager extends EntityManager<IssueWork> {
	
	void createOrUpdate(IssueWork work);
	
	List<IssueWork> query(User user, Issue issue, Date fromDate, Date toDate);
	
	List<IssueWork> query(@Nullable ProjectScope projectScope, EntityQuery<Issue> issueQuery, Date fromDate, Date toDate);
	
}
