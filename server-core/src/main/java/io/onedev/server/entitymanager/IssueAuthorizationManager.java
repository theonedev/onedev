package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueAuthorization;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueAuthorizationManager extends EntityManager<IssueAuthorization> {

	void authorize(Issue issue, User user);

	@Nullable
	IssueAuthorization find(Issue issue, User user);

}