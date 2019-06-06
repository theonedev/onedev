package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.model.Issue;

public class IssueCommitted extends IssueEvent {

	public IssueCommitted(Issue issue) {
		super(null, new Date(), issue);
	}

	@Override
	public boolean affectsBoards() {
		return false;
	}

}
