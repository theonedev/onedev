package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.ProjectScopedCommit;
import org.eclipse.jgit.lib.ObjectId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IssueReferencedFromCommitData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final String commitHash;
	
	public IssueReferencedFromCommitData(ProjectScopedCommit commit) {
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
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

	@Override
	public boolean affectsListing() {
		return false;
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
