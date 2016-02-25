package com.pmease.gitplex.core.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Password;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.UserBelonging;
import com.pmease.gitplex.core.util.validation.UserName;

@Entity
@Table(indexes={@Index(columnList="email"), @Index(columnList="fullName"), 
		@Index(columnList="noSpaceName"), @Index(columnList="noSpaceFullName")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)

/* 
 * use dynamic update as we do not want to change name while persisting user, 
 * as name of user can be referenced in other places, and we want to update 
 * these references in a transaction via UserManager.rename method 
 */  
@DynamicUpdate
@Editable
public class User extends AbstractUser implements ProtectedObject {

	private static final long serialVersionUID = 1L;

	public static final Long ROOT_ID = 1L;

	@Column(nullable=false)
	private String email;
	
	private String fullName;

	@Column(nullable=false)
	private String noSpaceName;
	
	private String noSpaceFullName;
	
	private Date avatarUploadDate;
	
	private int reviewEffort;
	
	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	@OneToMany(mappedBy="owner")
	private Collection<Depot> depots = new ArrayList<>();

	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<PullRequestReference> requestReferences = new ArrayList<>();
	
	@OneToMany(mappedBy="owner")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Team> teams = new ArrayList<Team>();
	
	@OneToMany(mappedBy="submitter")
	private Collection<PullRequest> submittedRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="assignee")
	private Collection<PullRequest> assignedRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="closeInfo.closedBy")
	private Collection<PullRequest> closedRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="reviewer")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Review> reviews = new ArrayList<Review>();
	
	@OneToMany(mappedBy="reviewer")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<ReviewInvitation> reviewInvitations = new ArrayList<>();

	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Comment> requestComments = new ArrayList<>();

	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<CommentReply> requestCommentReplies = new ArrayList<>();
	
    @OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
    private Collection<BranchWatch> branchWatches = new ArrayList<>();

    @OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
    private Collection<PullRequestWatch> requestWatches = new ArrayList<>();

    @OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
    private Collection<Notification> requestNotifications = new ArrayList<>();

    @OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
    private Collection<PullRequestVisit> requestVisits = new ArrayList<>();

    @Editable(name="Login Name", order=100)
	@UserName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}
	
    @Override
    public void setName(String name) {
    	super.setName(name);
    	noSpaceName = StringUtils.deleteWhitespace(name);
    }
    
	@Editable(order=200)
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
		noSpaceFullName = StringUtils.deleteWhitespace(fullName);
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
	@Length(min=5, message="Password length should not less than 5")
	public String getPassword() {
		return super.getPassword();
	}

	public Date getAvatarUploadDate() {
		return avatarUploadDate;
	}

	public void setAvatarUploadDate(Date avatarUploadDate) {
		this.avatarUploadDate = avatarUploadDate;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public int getReviewEffort() {
		return reviewEffort;
	}

	public void setReviewEffort(int reviewEffort) {
		this.reviewEffort = reviewEffort;
	}

	public Collection<Depot> getDepots() {
		return depots;
	}

	public void setDepots(Collection<Depot> depots) {
		this.depots = depots;
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

    public Collection<PullRequest> getAssignedRequests() {
		return assignedRequests;
	}

	public void setAssignedRequests(Collection<PullRequest> assignedRequests) {
		this.assignedRequests = assignedRequests;
	}

	public Collection<PullRequest> getClosedRequests() {
		return closedRequests;
	}

	public void setClosedRequests(Collection<PullRequest> closedRequests) {
		this.closedRequests = closedRequests;
	}

	public Collection<Comment> getRequestComments() {
		return requestComments;
	}

	public void setRequestComments(Collection<Comment> requestComments) {
		this.requestComments = requestComments;
	}

	public Collection<CommentReply> getRequestCommentReplies() {
		return requestCommentReplies;
	}

	public void setRequestCommentReplies(
			Collection<CommentReply> requestCommentReplies) {
		this.requestCommentReplies = requestCommentReplies;
	}

	public Collection<BranchWatch> getBranchWatches() {
		return branchWatches;
	}

	public void setBranchWatches(Collection<BranchWatch> branchWatches) {
		this.branchWatches = branchWatches;
	}

	public Collection<Notification> getRequestNotifications() {
		return requestNotifications;
	}

	public void setRequestNotifications(Collection<Notification> requestNotifications) {
		this.requestNotifications = requestNotifications;
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
	
	public Review.Result checkReviewSince(PullRequestUpdate update) {
		for (Review vote: update.listReviewsOnwards()) {
			if (vote.getReviewer().equals(this))
				return vote.getResult();
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
	
	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}

	public boolean isRoot() {
		return ROOT_ID.equals(getId());
	}

	public String getNoSpaceName() {
		return noSpaceName;
	}

	public String getNoSpaceFullName() {
		return noSpaceFullName;
	}
	
}
