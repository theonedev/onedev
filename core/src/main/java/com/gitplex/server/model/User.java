package com.gitplex.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

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
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.Password;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.util.validation.annotation.UserName;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@Entity
@Table(indexes={@Index(columnList="email"), @Index(columnList="fullName")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class User extends AbstractEntity implements AuthenticationInfo {

	private static final long serialVersionUID = 1L;

	public static final Long ROOT_ID = 1L;
	
    @Column(unique=true, nullable=false)
    private String name;

    @Column(length=1024)
    private String password;

	private String fullName;
	
	@Column(unique=true)
	private String email;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * branch and tag protection settings upon user renaming/deletion
	 */
	@Version
	private long version;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<UserAuthorization> authorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<Review> reviews = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<ReviewInvitation> reviewInvitations = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<BranchWatch> branchWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestWatch> requestWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestTask> requestTasks = new ArrayList<>();

    private transient Collection<Project> authorizedProjects;
    
    private transient Collection<Group> groups;
    
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
    
    @Editable(name="User Name", order=100)
	@UserName
	@NotEmpty
	public String getName() {
		return name;
	}
	
    public void setName(String name) {
    	this.name = name;
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

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setGroups(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Collection<PullRequestTask> getRequestTasks() {
		return requestTasks;
	}

	public void setRequestTasks(Collection<PullRequestTask> requestTasks) {
		this.requestTasks = requestTasks;
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

	public long getVersion() {
		return version;
	}

	public Collection<UserAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizedProjects(Collection<UserAuthorization> authorizations) {
		this.authorizations = authorizations;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public int compareTo(AbstractEntity entity) {
		User user = (User) entity;
		if (getDisplayName().equals(user.getDisplayName())) {
			return getId().compareTo(entity.getId());
		} else {
			return getDisplayName().compareTo(user.getDisplayName());
		}
	}

	public static User getForDisplay(@Nullable User user, @Nullable String userName) {
		if (user == null && userName != null) {
			user = new User();
			user.setName(userName);
		}
		return user;
	}

	public Collection<Project> getAuthorizedProjects() {
		if (authorizedProjects == null) {
			authorizedProjects = new HashSet<>();
			for (UserAuthorization authorization: getAuthorizations()) {
				authorizedProjects.add(authorization.getProject());
			}
		}
		return authorizedProjects;
	}
	
	public Collection<Group> getGroups() {
		if (groups == null) {
			groups = new HashSet<>();
			for (Membership membership: getMemberships()) {
				groups.add(membership.getGroup());
			}
		}
		return groups;
	}

	public UserFacade getFacade() {
		return new UserFacade(this);
	}
	
}
