package io.onedev.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;

public interface IssueCommentService extends EntityService<IssueComment> {

	void create(IssueComment comment);

	void create(IssueComment comment, Collection<String> notifiedEmailAddresses);
		
	void delete(User user, IssueComment comment);
	
	void update(IssueComment comment);
	
	List<IssueComment> query(User submitter, Date fromDate, Date toDate);

}
