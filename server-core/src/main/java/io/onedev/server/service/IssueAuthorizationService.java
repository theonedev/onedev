package io.onedev.server.service;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.User;

public interface IssueAuthorizationService extends EntityService<IssueAuthorization> {

	void authorize(Issue issue, User user);

    void createOrUpdate(IssueAuthorization authorization);
	
}