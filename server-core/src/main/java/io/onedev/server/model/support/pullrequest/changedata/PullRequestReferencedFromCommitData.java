package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueChangeData;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.ProjectScopedCommit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PullRequestReferencedFromCommitData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final ProjectScopedCommit commit;
	
	public PullRequestReferencedFromCommitData(ProjectScopedCommit commit) {
		this.commit = commit;
	}

	public ProjectScopedCommit getCommit() {
		return commit;
	}

	@Override
	public String getActivity() {
		return "referenced from commit";
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getCommit());
	}

}
