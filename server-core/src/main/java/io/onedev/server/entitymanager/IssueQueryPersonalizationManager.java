package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.ProjectScope;

import java.util.Collection;

public interface IssueQueryPersonalizationManager extends EntityManager<IssueQueryPersonalization> {
	
	IssueQueryPersonalization find(Project project, User user);

    void create(IssueQueryPersonalization personalization);

	void update(IssueQueryPersonalization personalization);
	
	Collection<IssueQueryPersonalization> query(ProjectScope projectScope);
	
}
