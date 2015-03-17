package com.pmease.gitplex.search;

import com.pmease.gitplex.core.model.Repository;

public class CommitIndexed {
	
	private final Repository repository;
	
	private final String commitHash;
	
	public CommitIndexed(Repository repository, String commitHash) {
		this.repository = repository;
		this.commitHash = commitHash;
	}

	public Repository getRepository() {
		return repository;
	}

	public String getCommitHash() {
		return commitHash;
	}
	
}
