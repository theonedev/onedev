package io.onedev.server.util;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;

public class ProjectAndBranch extends ProjectAndRevision {

	private static final long serialVersionUID = 1L;
	
	public ProjectAndBranch(Long projectId, String branch) {
		super(projectId, branch);
	}

	public ProjectAndBranch(Project project, String branch) {
		super(project, branch);
	}

	public ProjectAndBranch(String projectAndBranch) {
		super(projectAndBranch);
	}
	
	@Override
	public String getBranch() {
		return getRevision();
	}

	@Override
	protected String normalizeRevision() {
		return GitUtils.branch2ref(getBranch());
	}
	
}
