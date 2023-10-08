package io.onedev.server.manager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;

import javax.annotation.Nullable;
import java.util.List;

public interface IssueWorkManager extends EntityManager<IssueWork> {
	
	void create(IssueWork work);

    void update(IssueWork work);
	
	List<IssueWork> query(@Nullable ProjectScope projectScope, EntityQuery<Issue> issueQuery, long fromDay, long toDay);
	
}
