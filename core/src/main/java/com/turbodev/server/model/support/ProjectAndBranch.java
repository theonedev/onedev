package com.turbodev.server.model.support;

import com.turbodev.server.git.GitUtils;
import com.turbodev.server.model.Project;

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
