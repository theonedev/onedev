package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueWork;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueWorkManager extends EntityManager<IssueWork> {
	
	void create(IssueWork work);
	
}
