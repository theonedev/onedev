package io.onedev.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;

public interface PullRequestCommentService extends EntityService<PullRequestComment> {

	void create(PullRequestComment comment);

	void create(PullRequestComment comment, Collection<String> notifiedEmailAddresses);

	void update(PullRequestComment comment);

	void delete(User user, PullRequestComment comment);
	
	List<PullRequestComment> query(User submitter, Date fromDate, Date toDate);

}
