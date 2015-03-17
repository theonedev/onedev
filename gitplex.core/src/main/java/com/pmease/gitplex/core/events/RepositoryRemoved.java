package com.pmease.gitplex.core.events;

import com.pmease.gitplex.core.model.Repository;

public class RepositoryRemoved {

	private final Repository repository;
	
	public RepositoryRemoved(Repository repository) {
		this.repository = repository;
	}

	public Repository getRepository() {
		return repository;
	}
	
}
