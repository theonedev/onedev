package com.pmease.commons.product.pages;

import com.pmease.commons.product.model.Repository;
import com.pmease.commons.product.model.User;

public class RepositoryViewer {
	
	private User user;
	
	private Repository repository;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
}
