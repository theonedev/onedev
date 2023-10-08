package io.onedev.server.manager;

import java.util.Collection;

import io.onedev.server.model.IssueComment;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueCommentManager extends EntityManager<IssueComment> {

	void create(IssueComment comment, Collection<String> notifiedEmailAddresses);
	
	void delete(IssueComment comment);
	
	void update(IssueComment comment);
	
}
