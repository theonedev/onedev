package io.onedev.server.event.issue;

import java.util.Date;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Issue;

public class IssueCommitted extends IssueEvent {

	private final List<ObjectId> fixCommits;
	
	public IssueCommitted(Issue issue, List<ObjectId> fixCommits) {
		super(null, new Date(), issue);
		this.fixCommits = fixCommits;
	}

	@Override
	public boolean affectsBoards() {
		return false;
	}

	public List<ObjectId> getFixCommits() {
		return fixCommits;
	}

}
