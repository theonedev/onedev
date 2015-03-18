package com.pmease.gitplex.search;

import com.pmease.gitplex.core.model.Repository;

public class IndexRemoving {

	private final Repository repository;
	
	public IndexRemoving(Repository repository) {
		this.repository = repository;
	}

	public Repository getRepository() {
		return repository;
	}

}
