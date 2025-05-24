package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

	void create(PullRequestComment comment);

	void create(PullRequestComment comment, Collection<String> notifiedEmailAddresses);

	void update(PullRequestComment comment);
	
	List<PullRequestComment> query(User submitter, Date fromDate, Date toDate);

}
