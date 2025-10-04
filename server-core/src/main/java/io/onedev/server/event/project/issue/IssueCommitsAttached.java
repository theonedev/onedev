package io.onedev.server.event.project.issue;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Issue;

public class IssueCommitsAttached extends IssueEvent {

	private static final long serialVersionUID = 1L;
	
	public IssueCommitsAttached(Issue issue) {
		super(OneDev.getInstance(UserService.class).getSystem(), new Date(), issue);
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
