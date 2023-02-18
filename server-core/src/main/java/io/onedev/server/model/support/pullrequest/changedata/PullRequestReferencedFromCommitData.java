package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.ProjectScopedCommit;
import org.eclipse.jgit.lib.ObjectId;

public class PullRequestReferencedFromCommitData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final String commitHash;
	
	public PullRequestReferencedFromCommitData(ProjectScopedCommit commit) {
		this.projectId = commit.getProjectId();
		this.commitHash = commit.getCommitId().name();
	}

	public ProjectScopedCommit getCommit() {
		return new ProjectScopedCommit(projectId, ObjectId.fromString(commitHash));
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
