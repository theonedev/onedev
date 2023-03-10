package io.onedev.server.event.project;

import java.util.Date;

import io.onedev.server.model.Project;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class DefaultBranchChanged extends ProjectEvent implements CommitAware {
	
	private static final long serialVersionUID = 1L;

	private final String defaultBranch;
	
	public DefaultBranchChanged(Project project, String defaultBranch) {
		super(null, new Date(), project);
		this.defaultBranch = defaultBranch;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	@Override
	public String getActivity() {
		return "Default branch changed";
	}

	@Override
	public ProjectScopedCommit getCommit() {
		return new ProjectScopedCommit(getProject(), getProject().getObjectId(defaultBranch, true));
	}

}
