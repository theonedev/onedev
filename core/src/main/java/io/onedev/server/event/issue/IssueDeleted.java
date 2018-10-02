package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public class IssueDeleted extends IssueEvent {

	public IssueDeleted(User user, Issue issue) {
		super(user, new Date(), issue);
	}

	@Override
	public boolean affectsBoards() {
		return true;
	}

}