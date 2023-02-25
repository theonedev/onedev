package io.onedev.server.job.match;

import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;

public class JobMatchContext {

	private final Project project;
	
	@Nullable
	private final String branch;
	
	@Nullable
	private final ObjectId commitId;
	
	@Nullable
	private final User user;
	
	@Nullable
	private final String jobName;

	public JobMatchContext(Project project, @Nullable String branch, @Nullable ObjectId commitId,
						   @Nullable User user, @Nullable String jobName) {
		this.project = project;
		this.branch = branch;
		this.commitId = commitId;
		this.user = user;
		this.jobName = jobName;
	}

	public Project getProject() {
		return project;
	}

	@Nullable
	public String getBranch() {
		return branch;
	}

	@Nullable
	public ObjectId getCommitId() {
		return commitId;
	}

	@Nullable
	public User getUser() {
		return user;
	}
	
	@Nullable
	public String getJobName() {
		return jobName;
	}

	public boolean isSubmittedBy(Role role) {
		return SecurityUtils.isAssignedRole(user, project, role);
	}

}
