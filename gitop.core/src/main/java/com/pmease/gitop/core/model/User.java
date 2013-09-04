package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.pmease.commons.security.AbstractUser;
import com.pmease.gitop.core.model.gatekeeper.GateKeeper.CheckResult;
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
public class User extends AbstractUser implements ProtectedObject {

	private String description;
	
	@OneToMany(mappedBy="user")
	private Collection<TeamMembership> teamMemberships = new ArrayList<TeamMembership>();
	
	@OneToMany(mappedBy="user")
	private Collection<RoleMembership> roleMemberships = new ArrayList<RoleMembership>();
	
	@OneToMany(mappedBy="user")
	private Collection<MergeRequest> mergeRequests = new ArrayList<MergeRequest>();
	
	@OneToMany(mappedBy="owner")
	private Collection<Repository> repositories = new ArrayList<Repository>();

	@OneToMany(mappedBy="owner")
	private Collection<Team> teams = new ArrayList<Team>();
	
	@OneToMany(mappedBy="reviewer")
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	@OneToMany(mappedBy="reviewer")
	private Collection<PendingVote> pendingVotes = new ArrayList<PendingVote>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<TeamMembership> getTeamMemberships() {
		return teamMemberships;
	}

	public void setTeamMemberships(Collection<TeamMembership> teamMemberships) {
		this.teamMemberships = teamMemberships;
	}

	public Collection<RoleMembership> getRoleMemberships() {
		return roleMemberships;
	}

	public void setRoleMemberships(Collection<RoleMembership> roleMemberships) {
		this.roleMemberships = roleMemberships;
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

	public Collection<Vote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<Vote> votes) {
		this.votes = votes;
	}

	public Collection<PendingVote> getPendingVotes() {
		return pendingVotes;
	}

	public void setPendingVotes(Collection<PendingVote> pendingVotes) {
		this.pendingVotes = pendingVotes;
	}

	@Override
	public boolean has(ProtectedObject object) {
		if (object instanceof User) {
			User user = (User) object;
			return user.getId().equals(getId());
		} else if (object instanceof UserBelonging) {
			UserBelonging userBelonging = (UserBelonging) object;
			return userBelonging.getUser().getId().equals(getId());
		} else {
			return false;
		}
	}
	
	public CheckResult checkApprovalSince(MergeRequestUpdate update) {
		if (update.getRequest().getSubmitter().getId().equals(getId()))
			return CheckResult.ACCEPT;
		
		for (Vote vote: update.findVotesOnwards()) {
			if (vote.getReviewer().getId().equals(getId())) {
				if (vote.getResult() == Vote.Result.ACCEPT)
					return CheckResult.ACCEPT;
				else
					return CheckResult.REJECT;
			}
		}
		
		return CheckResult.PENDING;
	}

}
