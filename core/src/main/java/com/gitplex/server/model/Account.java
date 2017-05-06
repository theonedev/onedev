package com.gitplex.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamAuthorizationManager;
import com.gitplex.server.manager.TeamMembershipManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.security.protectedobject.AccountBelonging;
import com.gitplex.server.security.protectedobject.ProtectedObject;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.ExcludeValues;
import com.gitplex.server.util.editable.annotation.Password;
import com.gitplex.server.util.validation.annotation.AccountName;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

@Entity
@Table(indexes={
		@Index(columnList="email"), @Index(columnList="fullName"), 
		@Index(columnList="noSpaceName"), @Index(columnList="noSpaceFullName")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Account extends AbstractEntity implements AuthenticationInfo, ProtectedObject {

	private static final long serialVersionUID = 1L;

	public static final Long ADMINISTRATOR_ID = 1L;
	
    @Column(unique=true, nullable=false)
    private String name;

    @Column(length=1024)
    private String password;

    private boolean organization;
	
	private String fullName;
	
	/* used by user account */
	@Column(unique=true)
	private String email;
	
	/* used by organization account */
	@Column(nullable=false)
	private DepotPrivilege defaultPrivilege = DepotPrivilege.NONE;
	
	@Column(nullable=false)
	private String noSpaceName;
	
	private String noSpaceFullName;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * branch and tag protection settings upon account renaming/deletion
	 */
	@Version
	private long version;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<OrganizationMembership> organizations = new ArrayList<>();
	
	@OneToMany(mappedBy="organization", cascade=CascadeType.REMOVE)
	private Collection<OrganizationMembership> organizationMembers = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<TeamMembership> joinedTeams = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<UserAuthorization> authorizedDepots = new ArrayList<>();
	
	@OneToMany(mappedBy="account")
	private Collection<Depot> depots = new ArrayList<>();

	/* used by organization account */
	@OneToMany(mappedBy="organization", cascade=CascadeType.REMOVE)
	private Collection<Team> definedTeams = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReview> reviews = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<ReviewInvitation> reviewInvitations = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<BranchWatch> branchWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestWatch> requestWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestTask> requestTasks = new ArrayList<>();

    private transient Collection<TeamAuthorization> allTeamAuthorizationsInOrganization;
    
    private transient Collection<TeamMembership> allTeamMembershipsInOrganiation;
    
    private transient Collection<UserAuthorization> allUserAuthorizationsInOrganization;
    
    private transient Map<Account, OrganizationMembership> organizationMembersMap;
    
    @Override
    public PrincipalCollection getPrincipals() {
        return new SimplePrincipalCollection(getId(), "");
    }
    
    @Override
    public Object getCredentials() {
    	return password;
    }

    public Subject asSubject() {
    	return asSubject(getId());
    }

    public static Long getCurrentId() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        Preconditions.checkNotNull(principal);
        return (Long) principal;
    }

    public static PrincipalCollection asPrincipal(Long userId) {
        return new SimplePrincipalCollection(userId, "");
    }
    
    public static Subject asSubject(Long userId) {
    	WebSecurityManager securityManager = AppLoader.getInstance(WebSecurityManager.class);
        return new Subject.Builder(securityManager).principals(asPrincipal(userId)).buildSubject();
    }
    
    @Editable(name="Account Name", order=100)
	@AccountName
	@NotEmpty
	public String getName() {
		return name;
	}
	
    public void setName(String name) {
    	this.name = name;
    	noSpaceName = StringUtils.deleteWhitespace(name);
    }
    
	@Editable(order=150, autocomplete="new-password")
	@Password(confirmative=true)
	@NotEmpty
	public String getPassword() {
		return password;
	}

    /**
     * Set password of this user. 
     * 
     * @param password
     * 			password to set
     */
    public void setPassword(String password) {
    	if (password != null) {
    		this.password = password;
    	} else {
    		this.password = null;
    	}
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

	@Editable(order=370, description=""
			+ "Members will have this minimal privilege on all repositories "
			+ "in this organization. "
			+ "<ul>"
			+ "<li>None: No any permissions granted on repositories"
			+ "<li>Read: Able to pull and browse repositories"
			+ "<li>Write: Has full read privilege, and can also push to " 
			+ "repositories unless gatekeeper prevents")
	@ExcludeValues("ADMIN")
	@NotNull
	public DepotPrivilege getDefaultPrivilege() {
		return defaultPrivilege;
	}

	public void setDefaultPrivilege(DepotPrivilege defaultPrivilege) {
		this.defaultPrivilege = defaultPrivilege;
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

	public Collection<PullRequestTask> getRequestTasks() {
		return requestTasks;
	}

	public void setRequestTasks(Collection<PullRequestTask> requestTasks) {
		this.requestTasks = requestTasks;
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

	@Nullable
	public PullRequestReview getReviewAfter(PullRequestUpdate update) {
		for (PullRequestReview review: update.getRequest().getReviews()) {
			if (review.getUser().equals(this) && review.getUpdate() != null 
					&& review.getUpdate().getId()>=update.getId()) {
				return review;
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

	public Collection<UserAuthorization> getAuthorizedDepots() {
		return authorizedDepots;
	}

	public void setAuthorizedDepots(Collection<UserAuthorization> authorizedDepots) {
		this.authorizedDepots = authorizedDepots;
	}

	public Collection<TeamAuthorization> getAllTeamAuthorizationsInOrganization() {
		if (allTeamAuthorizationsInOrganization == null) {
			allTeamAuthorizationsInOrganization = GitPlex.getInstance(TeamAuthorizationManager.class).findAll(this);
		}
		return allTeamAuthorizationsInOrganization;
	}

	public Collection<UserAuthorization> getAllUserAuthorizationsInOrganization() {
		if (allUserAuthorizationsInOrganization == null) {
			allUserAuthorizationsInOrganization = GitPlex.getInstance(UserAuthorizationManager.class).findAll(this);
		}
		return allUserAuthorizationsInOrganization;
	}
	
	public Collection<TeamMembership> getAllTeamMembershipsInOrganiation() {
		if (allTeamMembershipsInOrganiation == null) {
			allTeamMembershipsInOrganiation = GitPlex.getInstance(TeamMembershipManager.class).findAll(this);
		}
		return allTeamMembershipsInOrganiation;
	}

	public Map<Account, OrganizationMembership> getOrganizationMembersMap() {
		if (organizationMembersMap == null) {
			organizationMembersMap = new HashMap<>();
			for (OrganizationMembership membership: getOrganizationMembers()) {
				organizationMembersMap.put(membership.getUser(), membership);
			}
		}
		return organizationMembersMap;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public int compareTo(AbstractEntity entity) {
		Account account = (Account) entity;
		if (getDisplayName().equals(account.getDisplayName())) {
			return getId().compareTo(entity.getId());
		} else {
			return getDisplayName().compareTo(account.getDisplayName());
		}
	}

	public static Set<String> getOrganizationExcludeProperties() {
		return Sets.newHashSet("email", "password");
	}
	
	public static Set<String> getUserExcludeProperties() {
		return Sets.newHashSet("defaultPrivilege", "description");
	}

	public static Account getForDisplay(@Nullable Account account, @Nullable String accountName) {
		if (account == null && accountName != null) {
			account = new Account();
			account.setName(accountName);
		}
		return account;
	}
	
}
