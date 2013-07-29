package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.pmease.commons.security.AbstractUser;

/**
 * This class represents an user (or project) in the system. In Gitop, users and projects
 * are the same thing. To create a project, one can register a new user account representing 
 * the project itself, and then under that user account, create repositories and organize 
 * teams to do project cooperation. 
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Account extends AbstractUser {

	@OneToMany(mappedBy="account")
	private Collection<Membership> memberships;
	
	@OneToMany(mappedBy="account")
	private Collection<Authorization> authorizations;

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

}
