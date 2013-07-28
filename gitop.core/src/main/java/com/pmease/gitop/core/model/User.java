package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.FetchMode;

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
public class User extends AbstractUser {

	@OneToMany(mappedBy="user")
	private Collection<Membership> memberships;
	
	@OneToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_USER_AUTH")
	private Authorization authorization;

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Authorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(Authorization authorization) {
		this.authorization = authorization;
	}

}
