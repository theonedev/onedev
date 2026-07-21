package io.onedev.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;

public interface IssueCommentService extends EntityService<IssueComment> {

	void create(IssueComment comment);

	void create(IssueComment comment, Collection<String> listeningEmailAddresses);

	void create(User user, Issue issue, String content);
		
	void delete(User user, IssueComment comment);
	
	void update(IssueComment comment);

	@Nullable
	IssueComment findByMessageId(String messageId);
	
	List<IssueComment> query(User submitter, Date fromDate, Date toDate);

}
