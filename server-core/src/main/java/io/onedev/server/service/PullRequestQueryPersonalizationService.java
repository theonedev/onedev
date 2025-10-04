package io.onedev.server.service;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestQueryPersonalization;
import io.onedev.server.model.User;

public interface PullRequestQueryPersonalizationService extends EntityService<PullRequestQueryPersonalization> {
	
	PullRequestQueryPersonalization find(Project project, User user);

    void createOrUpdate(PullRequestQueryPersonalization personalization);
	
    void delete(PullRequestQueryPersonalization personalization);
}
