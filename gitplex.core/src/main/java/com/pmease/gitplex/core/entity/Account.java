package com.pmease.gitplex.core.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Markdown;
import com.pmease.commons.wicket.editable.annotation.Password;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.TeamMembershipManager;
import com.pmease.gitplex.core.permission.object.AccountBelonging;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;
import com.pmease.gitplex.core.util.validation.AccountName;

@Entity
@Table(indexes={@Index(columnList="email"), @Index(columnList="fullName"), 
		@Index(columnList="noSpaceName"), @Index(columnList="noSpaceFullName")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Account extends AbstractUser implements ProtectedObject {

	private static final long serialVersionUID = 1L;

	public static final Long ADMINISTRATOR_ID = 1L;

	private boolean organization;
	
	private String fullName;
	
	/* used by user account */
	@Column(unique=true)
	private String email;
	
	/* used by user account */
	private int reviewEffort;
	
	/* used by organization account */
	private String description;
	
	/* used by organization account */
	@Column(nullable=false)
	private DepotPrivilege defaultPrivilege = DepotPrivilege.NONE;
	
	@Column(nullable=false)
	private String noSpaceName;
	
	private String noSpaceFullName;
	
	private Date avatarUploadDate;
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * gate keepers and integration policies upon account renaming/deletion
	 */
	@Version
	private long version;
	
	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<OrganizationMembership> organizations = new ArrayList<>();
	
	@OneToMany(mappedBy="organization")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<OrganizationMembership> organizationMembers = new ArrayList<>();
	
	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<TeamMembership> joinedTeams = new ArrayList<>();
	
	@OneToMany(mappedBy="account")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Depot> depots = new ArrayList<>();

	/* used by organization account */
	@OneToMany(mappedBy="organization")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Team> definedTeams = new ArrayList<>();
	
	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<PullRequestReference> requestReferences = new ArrayList<>();
	
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
    
    private transient Collection<Authorization> allAuthorizationsInOrganization;
    
    private transient Collection<TeamMembership> allTeamMembershipsInOrganiation;
    
    private transient Collection<Account> organizationAdministrators;

    @Editable(name="Name", order=100)
	@AccountName
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
	
	public boolean isOrganization() {
		return organization;
	}

	public void setOrganization(boolean organization) {
		this.organization = organization;
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

	@Editable(order=350)
	@Markdown
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=370, description=""
			+ "Members will have this minimal privilege on all repositories "
			+ "in this organization. "
			+ "<ul>"
			+ "<li>None: No any permissions granted on repositories"
			+ "<li>Read: Able to pull and browse repositories"
			+ "<li>Write: Has full read privilege, and can also push to" 
			+ "repositories unless gate keeper prevents"
			+ "<li>Admin: Has full write privilege, and can also edit"  
			+ "settings of repositories")
	@NotNull
	public DepotPrivilege getDefaultPrivilege() {
		return defaultPrivilege;
	}

	public void setDefaultPrivilege(DepotPrivilege defaultPrivilege) {
		this.defaultPrivilege = defaultPrivilege;
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

	public Collection<OrganizationMembership> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Collection<OrganizationMembership> organizations) {
		this.organizations = organizations;
	}

	public Collection<OrganizationMembership> getOrganizationMembers() {
		return organizationMembers;
	}

	public void setOrganizationMemberships(Collection<OrganizationMembership> organizationMembers) {
		this.organizationMembers = organizationMembers;
	}

	public Collection<TeamMembership> getJoinedTeams() {
		return joinedTeams;
	}

	public void setJoinedTeams(Collection<TeamMembership> joinedTeams) {
		this.joinedTeams = joinedTeams;
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

	public Collection<Team> getDefinedTeams() {
		return definedTeams;
	}

	public void setDefinedTeams(Collection<Team> definedTeams) {
		this.definedTeams = definedTeams;
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
		if (object instanceof Account) {
			Account user = (Account) object;
			return user.equals(this);
		} else if (object instanceof AccountBelonging) {
			AccountBelonging userBelonging = (AccountBelonging) object;
			return userBelonging.getAccount().equals(this);
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
	
	public boolean matches(@Nullable String searchTerm) {
		if (searchTerm == null)
			searchTerm = "";
		else
			searchTerm = searchTerm.toLowerCase().trim();
		
		return getName().toLowerCase().contains(searchTerm) 
				|| fullName!=null && fullName.toLowerCase().contains(searchTerm);
	}

	public boolean isAdministrator() {
		return ADMINISTRATOR_ID.equals(getId());
	}

	public String getNoSpaceName() {
		return noSpaceName;
	}

	public String getNoSpaceFullName() {
		return noSpaceFullName;
	}

	public long getVersion() {
		return version;
	}

	public Collection<Authorization> getAllAuthorizationsInOrganization() {
		if (allAuthorizationsInOrganization == null) {
			allAuthorizationsInOrganization = GitPlex.getInstance(AuthorizationManager.class).query(this);
		}
		return allAuthorizationsInOrganization;
	}

	public Collection<TeamMembership> getAllTeamMembershipsInOrganiation() {
		if (allTeamMembershipsInOrganiation == null) {
			allTeamMembershipsInOrganiation = GitPlex.getInstance(TeamMembershipManager.class).query(this);
		}
		return allTeamMembershipsInOrganiation;
	}

	public Collection<Account> getOrganizationAdministrators() {
		if (organizationAdministrators == null) {
			organizationAdministrators = new HashSet<>();
			for (OrganizationMembership membership: getOrganizationMembers()) {
				if (membership.isAdmin()) {
					organizationAdministrators.add(membership.getUser());
				}
			}
		}
		return organizationAdministrators;
	}
}
