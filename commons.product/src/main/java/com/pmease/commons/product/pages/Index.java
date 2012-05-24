package com.pmease.commons.product.pages;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.ComponentSource;

import com.pmease.commons.product.model.Repository;
import com.pmease.commons.product.model.User;

public class Index {
	
	@Property
	private User user;
	
	@Property
	private Repository repository;
	
	@Inject
	private ComponentSource componentSource;
	
	@InjectComponent
	private Zone myZone;
	
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		
		User user = new User();
		user.setName("robin");
		user.setEmail("robin@pmease.com");
		users.add(user);

		user = new User();
		user.setName("steve");
		user.setEmail("steve@pmease.com");
		users.add(user);

		return users;
	}
	
	void onActivate(String userName) {
		user = new User();
		user.setName(userName);
	}
	
	void onActivate(String userName, String repositoryName) {
		user = new User();
		user.setName(userName);
		
		repository = new Repository();
		repository.setName(repositoryName);
	}
	
	public UserViewer getUserViewer() {
		UserViewer viewer = (UserViewer) componentSource.getPage(UserViewer.class);
		viewer.setUser(user);
		return viewer;
	}
	
	public RepositoryViewer getRepositoryViewer() {
		RepositoryViewer viewer = (RepositoryViewer) componentSource.getPage(RepositoryViewer.class);
		viewer.setUser(user);
		viewer.setRepository(repository);
		return viewer;
	}
	
	Object onActionFromEmailLink(String userName) {
		user = new User();
		user.setName(userName);
		user.setEmail(userName + "@pmease.com");
		return myZone;
	}
}
