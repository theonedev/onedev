package com.pmease.gitplex.core.model;

import com.pmease.commons.git.GitUtils;

public class RepoAndBranch extends RepoAndRevision {

	private static final long serialVersionUID = 1L;
	
	public RepoAndBranch(Long repoId, String branch) {
		super(repoId, branch);
	}

	public RepoAndBranch(Repository repository, String branch) {
		super(repository, branch);
	}

	public RepoAndBranch(String repoAndBranch) {
		super(repoAndBranch);
	}
	
	public String getBranch() {
		return getRevision();
	}

	@Override
	protected String normalizeRevision() {
		return GitUtils.branch2ref(getBranch());
	}
	
}
