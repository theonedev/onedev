package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.util.validation.annotation.UserName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Entity
@Table(indexes={@Index(columnList="email"), @Index(columnList="fullName")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class User extends AbstractEntity implements AuthenticationInfo {

	private static final long serialVersionUID = 1L;

	public static final Long ROOT_ID = 1L;
	
    @Column(unique=true, nullable=false)
    private String name;

    @Column(length=1024, nullable=false)
	@JsonView(DefaultView.class)
    private String password;

	private String fullName;
	
	@Column(unique=true, nullable=false)
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
	private Collection<PullRequestReview> reviews = new ArrayList<>();
	
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestWatch> requestWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueWatch> issueWatches = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueQuerySetting> issueQuerySettings = new ArrayList<>();
    
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
    	if (RequestCycle.get() != null) {
        	ServletRequest request = (ServletRequest) RequestCycle.get().getRequest().getContainerRequest();
        	ServletResponse response = (ServletResponse) RequestCycle.get().getResponse().getContainerResponse();
            return new WebSubject.Builder(securityManager, request, response).principals(asPrincipal(userId)).buildSubject();
    	} else {
            return new Subject.Builder(securityManager).principals(asPrincipal(userId)).buildSubject();
    	}
    }
    
    @Editable(name="Login Name", order=100)
	@UserName
	@NotEmpty
	public String getName() {
		return name;
	}
	
    public void setName(String name) {
    	this.name = name;
    }
    
	@Editable(order=150)
	@Password(confirmative=true, autoComplete="new-password")
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
    	this.password = password;
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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
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

	public Collection<IssueQuerySetting> getIssueQuerySettings() {
		return issueQuerySettings;
	}

	public void setIssueQuerySettings(Collection<IssueQuerySetting> issueQuerySettings) {
		this.issueQuerySettings = issueQuerySettings;
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

	public UserFacade getFacade() {
		return new UserFacade(this);
	}

}
