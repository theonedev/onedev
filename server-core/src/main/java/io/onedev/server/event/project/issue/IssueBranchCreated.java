package io.onedev.server.event.project.issue;

import java.io.Serial;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;

public class IssueBranchCreated extends IssueEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String branch;

	public IssueBranchCreated(@Nullable User user, Issue issue, String branch) {
		super(user != null ? user : OneDev.getInstance(UserService.class).getSystem(), new Date(), issue);
		this.branch = branch;
	}

	public String getBranch() {
		return branch;
	}

	@Override
	public boolean affectsListing() {
		return false;
	}

	@Override
	public String getActivity() {
		return "branch '" + branch + "' created";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
