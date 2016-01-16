package com.pmease.gitplex.core.model;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

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
	
	public String getFQN() {
		return getRepository().getBranchFQN(getBranch());		
	}
	
	@Nullable
	public String getHead(boolean mustExist) {
		ObjectId commitId = getRepository().getObjectId(GitUtils.branch2ref(getRevision()), mustExist);
		return commitId!=null?commitId.name():null;
	}
	
	public String getHead() {
		return getHead(true);
	}
	
}
