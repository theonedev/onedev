package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;

import javax.annotation.Nullable;
import java.util.List;

public interface IssueWorkManager extends EntityManager<IssueWork> {
	
	void createOrUpdate(IssueWork work);
	
	List<IssueWork> query(User user, Issue issue, long day);
	
	List<IssueWork> query(@Nullable ProjectScope projectScope, EntityQuery<Issue> issueQuery, long fromDay, long toDay);
	
}
