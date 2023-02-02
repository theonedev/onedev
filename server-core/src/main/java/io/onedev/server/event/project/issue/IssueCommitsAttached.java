package io.onedev.server.event.project.issue;

import io.onedev.server.model.Issue;

import java.util.Date;

public class IssueCommitsAttached extends IssueEvent {

	private static final long serialVersionUID = 1L;
	
	public IssueCommitsAttached(Issue issue) {
		super(null, new Date(), issue);
	}

	@Override
	public boolean affectsListing() {
		return false;
	}
	
	@Override
	public String getActivity() {
		return "commits attached";
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}
