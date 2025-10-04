package io.onedev.server.service;

import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;

import java.util.Collection;

public interface IssueQueryPersonalizationService extends EntityService<IssueQueryPersonalization> {
	
	IssueQueryPersonalization find(Project project, User user);

    void createOrUpdate(IssueQueryPersonalization personalization);
	
	Collection<IssueQueryPersonalization> query(ProjectScope projectScope);
	
}
