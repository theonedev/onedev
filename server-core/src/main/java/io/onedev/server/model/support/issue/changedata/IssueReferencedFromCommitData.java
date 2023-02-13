package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.ProjectScopedCommit;
import org.eclipse.jgit.lib.ObjectId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IssueReferencedFromCommitData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final ProjectScopedCommit commit;
	
	public IssueReferencedFromCommitData(ProjectScopedCommit commit) {
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
