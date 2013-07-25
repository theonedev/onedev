package com.pmease.gitop.core.model;

import javax.persistence.Entity;

import com.pmease.commons.security.AbstractUser;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class User extends AbstractUser {
	
	private boolean project;

	/**
	 * Whether or not this user represents a project. 
	 * 
	 * @return
	 */
	public boolean isProject() {
		return project;
	}

	public void setProject(boolean project) {
		this.project = project;
	}
	
}
