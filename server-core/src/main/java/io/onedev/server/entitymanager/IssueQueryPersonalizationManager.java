package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueQueryPersonalizationManager extends EntityManager<IssueQueryPersonalization> {
	
	IssueQueryPersonalization find(Project project, User user);
	
}
