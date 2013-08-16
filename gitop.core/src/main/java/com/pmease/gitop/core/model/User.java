package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.security.AbstractUser;
import com.pmease.gitop.core.model.permission.object.ProtectedObject;
import com.pmease.gitop.core.model.permission.object.UserBelonging;

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
public class User extends AbstractUser implements ProtectedObject, Permission {

	@OneToMany(mappedBy="user")
	private Collection<TeamMembership> memberships;
	
	@OneToMany(mappedBy="user")
	private Collection<MergeRequest> mergeRequests;
	
	@OneToMany(mappedBy="owner")
	private Collection<Repository> repositories;

	@OneToMany(mappedBy="owner")
	private Collection<Team> teams;

	public Collection<TeamMembership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<TeamMembership> memberships) {
		this.memberships = memberships;
	}

	public Collection<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(Collection<Repository> repositories) {
		this.repositories = repositories;
	}

	public Collection<Team> getTeams() {
		return teams;
	}

	public void setTeams(Collection<Team> teams) {
		this.teams = teams;
	}

	public Collection<MergeRequest> getMergeRequests() {
		return mergeRequests;
	}

	public void setMergeRequests(Collection<MergeRequest> mergeRequests) {
		this.mergeRequests = mergeRequests;
	}

	@Override
	public boolean has(ProtectedObject object) {
		if (object instanceof User) {
			User user = (User) object;
			return user.getId().equals(getId());
		} else if (object instanceof UserBelonging) {
			UserBelonging userBelonging = (UserBelonging) object;
			return userBelonging.getOwner().getId().equals(getId());
		} else {
			return false;
		}
	}

	@Override
	public boolean implies(Permission permission) {
		for (TeamMembership each: getMemberships()) {
			if (each.getGroup().implies(permission))
				return true;
		}
		
		return false;
	}

}
