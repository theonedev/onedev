package com.pmease.gitplex.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.UserBelonging;
import com.pmease.gitplex.core.validation.UserName;

@SuppressWarnings("serial")
@Entity
@Editable
public class User extends AbstractUser implements ProtectedObject {

	public static final Long ROOT_ID = 1L;
	
	@Column(nullable=false, unique=true)
	private String email;
	
	private String fullName;
	
	private boolean admin;
	
	private Date avatarUpdateDate;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<CommitComment> commitComments = new ArrayList<>();

	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	@OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	private Collection<Repository> repositories = new ArrayList<>();

	@OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	private Collection<Team> teams = new ArrayList<Team>();
	
	@OneToMany(mappedBy="submitter")
	private Collection<PullRequest> submittedRequests = new ArrayList<>();

	@OneToMany(mappedBy="user")
	private Collection<PullRequestUpdate> updates = new ArrayList<>();

	@OneToMany(mappedBy="voter", cascade=CascadeType.REMOVE)
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	@OneToMany(mappedBy="voter", cascade=CascadeType.REMOVE)
	private Collection<VoteInvitation> voteInvitations = new ArrayList<>();

	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PullRequestComment> requestComments = new ArrayList<>();

	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PullRequestCommentReply> requestCommentReplies = new ArrayList<>();
	
	@Lob
	@Column(nullable=false)
	private ArrayList<ChangeSubscription> changeSubscriptions = new ArrayList<>();

	@Editable(order=100)
	@UserName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Editable(order=200)
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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
	@Length(min=5)
	public String getPassword() {
		return super.getPassword();
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public Date getAvatarUpdateDate() {
		return avatarUpdateDate;
	}

	public void setAvatarUpdateDate(Date avatarUpdateDate) {
		this.avatarUpdateDate = avatarUpdateDate;
	}

	public Collection<CommitComment> getCommitComments() {
		return commitComments;
	}

	public void setCommitComments(Collection<CommitComment> commitComments) {
		this.commitComments = commitComments;
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
	 * Get pull requests submitted by this user.
	 * 
	 * @return
	 *			collection of pull requests submitted by this user
	 */
	public Collection<PullRequest> getSubmittedRequests() {
		return submittedRequests;
	}

	public void setSubmittedRequests(Collection<PullRequest> submittedRequests) {
		this.submittedRequests = submittedRequests;
	}

	public Collection<PullRequestUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<PullRequestUpdate> updates) {
		this.updates = updates;
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

    public Collection<PullRequestComment> getRequestComments() {
		return requestComments;
	}

	public void setRequestComments(Collection<PullRequestComment> requestComments) {
		this.requestComments = requestComments;
	}

	public Collection<PullRequestCommentReply> getRequestCommentReplies() {
		return requestCommentReplies;
	}

	public void setCommentReplies(Collection<PullRequestCommentReply> requestCommentReplies) {
		this.requestCommentReplies = requestCommentReplies;
	}

	public ArrayList<ChangeSubscription> getChangeSubscriptions() {
		return changeSubscriptions;
	}

	public void setChangeSubscriptions(
			ArrayList<ChangeSubscription> changeSubscriptions) {
		this.changeSubscriptions = changeSubscriptions;
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
		if (this.equals(update.getRequest().getSubmitter()))
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
		return Objects.toStringHelper(this)
				.add("name", getName())
				.toString();
	}
	
	public PersonIdent asPerson() {
		return new PersonIdent(getName(), getEmail());
	}
	
	public File getLocalAvatar() {
		return new File(Bootstrap.getSiteDir(), "avatars/" + getId());
	}
	
	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}

	public boolean isRoot() {
		return ROOT_ID.equals(getId());
	}
	
}
