package com.pmease.commons.product.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Property;

import com.pmease.commons.product.model.Repository;
import com.pmease.commons.product.model.User;

public class UserViewer {
	
	private User user;
	
	@SuppressWarnings("unused")
	@Property
	private Repository repository;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Repository> getRepositories() {
		List<Repository> repositories = new ArrayList<Repository>();
		Repository repository = new Repository();
		repository.setName("QuickBuild");
		repository.setDescription("QuickBuild Trunk");
		repositories.add(repository);
		
		repository = new Repository();
		repository.setName("Gitop");
		repository.setDescription("Gitop Trunk");
		repositories.add(repository);
		
		return repositories;
	}

}
