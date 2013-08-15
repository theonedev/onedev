package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.pmease.commons.security.AbstractUser;

/**
 * This class represents either a project or an user in the system. 
 * <p>
 * In Gitop, users and projects are the same thing. 
 * If necessary, you can always treat an user account as a project account. 
 * {@link Repository} and {@link Team} are always created under a specific account.  
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

	@OneToMany(mappedBy="account")
	private Collection<Authorization> mergeRequests;

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

	public Collection<Authorization> getMergeRequests() {
		return mergeRequests;
	}

	public void setMergeRequests(Collection<Authorization> mergeRequests) {
		this.mergeRequests = mergeRequests;
	}

}
