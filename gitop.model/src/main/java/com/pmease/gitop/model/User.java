package com.pmease.gitop.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitop.model.permission.object.ProtectedObject;
import com.pmease.gitop.model.permission.object.UserBelonging;
import com.pmease.gitop.model.validation.UserName;

@SuppressWarnings("serial")
@Entity
@Editable
public class User extends AbstractUser implements ProtectedObject {

	@Column(nullable=false, unique=true)
	private String email;
	
	private String displayName;
	
	private boolean admin;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<Membership> memberships = new ArrayList<Membership>();
	
	@OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	private Collection<Repository> repositories = new ArrayList<Repository>();

	@OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	private Collection<Team> teams = new ArrayList<Team>();
	
	@OneToMany(mappedBy="creator")
	private Collection<Branch> branches = new ArrayList<Branch>();
	
	@OneToMany(mappedBy="submittedBy")
	private Collection<PullRequest> submittedRequests = new ArrayList<>();

	@OneToMany(mappedBy="closeInfo.closedBy")
	private Collection<PullRequest> closedRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="user")
	private Collection<PullRequestUpdate> updates = new ArrayList<>();

	@OneToMany(mappedBy="voter", cascade=CascadeType.REMOVE)
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	@OneToMany(mappedBy="voter", cascade=CascadeType.REMOVE)
	private Collection<VoteInvitation> voteInvitations = new ArrayList<VoteInvitation>();

	@Editable(order=100)
	@UserName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Editable(order=200)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Editable(order=300)
	@NotEmpty
	@Email
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Editable(order=400)
	@Password(confirmative=true)
	@NotEmpty
	public String getPassword() {
		return getPasswordHash();
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
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

	/**
	 * Get created branches.
	 * 
	 * @return
	 * 			collection of created branches
	 */
	public Collection<Branch> getBranches() {
		return branches;
	}

	public void setBranches(Collection<Branch> branches) {
		this.branches = branches;
	}

	/**
	 * Get pull requests submitted by this user.
	 * 
	 * @return
	 *			collection of pull requests submitted by this user
	 */
	public Collection<PullRequest> getSubmittedRequests() {
		return submittedRequests;
	}

	public void setSubmittedRequests(Collection<PullRequest> submittedRequests) {
		this.closedRequests = submittedRequests;
	}

	/**
	 * Get pull requests closed by this user.
	 * 
	 * @return
	 * 			collection of pull requests closed by this user
	 */
	public Collection<PullRequest> getClosedRequests() {
		return closedRequests;
	}

	public Collection<PullRequestUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<PullRequestUpdate> updates) {
		this.updates = updates;
	}

	public void setClosedRequests(Collection<PullRequest> closedRequests) {
		this.closedRequests = closedRequests;
	}

	public Collection<Vote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<Vote> votes) {
		this.votes = votes;
	}

	public Collection<VoteInvitation> getVoteInvitations() {
		return voteInvitations;
	}

	public void setVoteInvitations(Collection<VoteInvitation> voteInvitations) {
		this.voteInvitations = voteInvitations;
	}

    @Override
	public boolean has(ProtectedObject object) {
		if (object instanceof User) {
			User user = (User) object;
			return user.equals(this);
		} else if (object instanceof UserBelonging) {
			UserBelonging userBelonging = (UserBelonging) object;
			return userBelonging.getUser().equals(this);
		} else {
			return false;
		}
	}
	
	public Vote.Result checkVoteSince(PullRequestUpdate update) {
		if (this.equals(update.getRequest().getSubmittedBy()))
			return Vote.Result.APPROVE;
		
		for (Vote vote: update.listVotesOnwards()) {
			if (vote.getVoter().equals(this)) {
				return vote.getResult();
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public PersonIdent asPerson() {
		return new PersonIdent(getName(), getEmail());
	}

	public File getLocalAvatar() {
		return new File(Bootstrap.installDir, "avatars/" + getId());
	}
	
}
