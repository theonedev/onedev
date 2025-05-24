package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueCommentManager extends EntityManager<IssueComment> {

	void create(IssueComment comment);

	void create(IssueComment comment, Collection<String> notifiedEmailAddresses);
	
	void delete(IssueComment comment);
	
	void update(IssueComment comment);
	
	List<IssueComment> query(User submitter, Date fromDate, Date toDate);

}
