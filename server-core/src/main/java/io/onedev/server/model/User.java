package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
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

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.model.support.NamedBuildQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.util.validation.annotation.UserName;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;
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
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * branch and tag protection settings upon user renaming/deletion
	 */
	@Version
	private long version;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> projectAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReview> reviews = new ArrayList<>();
	
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestWatch> requestWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueWatch> issueWatches = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueQuerySetting> projectIssueQuerySettings = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<BuildQuerySetting> projectBuildQuerySettings = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestQuerySetting> projectPullRequestQuerySettings = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<CommitQuerySetting> projectCommitQuerySettings = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<CodeCommentQuerySetting> projectCodeCommentQuerySettings = new ArrayList<>();
    
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedIssueQuery> userIssueQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userIssueQueryWatches = new LinkedHashMap<>();
	
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> issueQueryWatches = new LinkedHashMap<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedPullRequestQuery> userPullRequestQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userPullRequestQueryWatches = new LinkedHashMap<>();
	
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> pullRequestQueryWatches = new LinkedHashMap<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedBuildQuery> userBuildQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> userBuildQuerySubscriptions = new LinkedHashSet<>();
	
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> buildQuerySubscriptions = new LinkedHashSet<>();
	
    private transient Collection<Group> groups;
    
	public QuerySetting<NamedIssueQuery> getIssueQuerySetting() {
		return new QuerySetting<NamedIssueQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedIssueQuery> getUserQueries() {
				return userIssueQueries;
			}

			@Override
			public void setUserQueries(ArrayList<NamedIssueQuery> userQueries) {
				userIssueQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedIssueQuery> getQueryWatchSupport() {
				return new QueryWatchSupport<NamedIssueQuery>() {

					@Override
					public LinkedHashMap<String, Boolean> getUserQueryWatches() {
						return userIssueQueryWatches;
					}

					@Override
					public LinkedHashMap<String, Boolean> getQueryWatches() {
						return issueQueryWatches;
					}
					
				};
			}

			@Override
			public QuerySubscriptionSupport<NamedIssueQuery> getQuerySubscriptionSupport() {
				return null;
			}
			
		};
	}
	
	public void setBuildQuerySetting(QuerySetting<NamedBuildQuery> querySetting) {
		buildQuerySubscriptions = querySetting.getQuerySubscriptionSupport().getQuerySubscriptions();
		userBuildQuerySubscriptions = querySetting.getQuerySubscriptionSupport().getUserQuerySubscriptions();
		userBuildQueries = querySetting.getUserQueries();
	}
	
	public void setIssueQuerySetting(QuerySetting<NamedIssueQuery> querySetting) {
		issueQueryWatches = querySetting.getQueryWatchSupport().getQueryWatches();
		userIssueQueryWatches = querySetting.getQueryWatchSupport().getUserQueryWatches();
		userIssueQueries = querySetting.getUserQueries();
	}
	
	public void setPullRequestQuerySetting(QuerySetting<NamedPullRequestQuery> querySetting) {
		pullRequestQueryWatches = querySetting.getQueryWatchSupport().getQueryWatches();
		userPullRequestQueryWatches = querySetting.getQueryWatchSupport().getUserQueryWatches();
		userPullRequestQueries = querySetting.getUserQueries();
	}
	
	public QuerySetting<NamedPullRequestQuery> getPullRequestQuerySetting() {
		return new QuerySetting<NamedPullRequestQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedPullRequestQuery> getUserQueries() {
				return userPullRequestQueries;
			}

			@Override
			public void setUserQueries(ArrayList<NamedPullRequestQuery> userQueries) {
				userPullRequestQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedPullRequestQuery> getQueryWatchSupport() {
				return new QueryWatchSupport<NamedPullRequestQuery>() {

					@Override
					public LinkedHashMap<String, Boolean> getUserQueryWatches() {
						return userPullRequestQueryWatches;
					}

					@Override
					public LinkedHashMap<String, Boolean> getQueryWatches() {
						return pullRequestQueryWatches;
					}
					
				};
			}

			@Override
			public QuerySubscriptionSupport<NamedPullRequestQuery> getQuerySubscriptionSupport() {
				return null;
			}
			
		};
	}
	
	public QuerySetting<NamedBuildQuery> getBuildQuerySetting() {
		return new QuerySetting<NamedBuildQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedBuildQuery> getUserQueries() {
				return userBuildQueries;
			}

			@Override
			public void setUserQueries(ArrayList<NamedBuildQuery> userQueries) {
				userBuildQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedBuildQuery> getQueryWatchSupport() {
				return null;
			}

			@Override
			public QuerySubscriptionSupport<NamedBuildQuery> getQuerySubscriptionSupport() {
				return new QuerySubscriptionSupport<NamedBuildQuery>() {

					@Override
					public LinkedHashSet<String> getUserQuerySubscriptions() {
						return userBuildQuerySubscriptions;
					}

					@Override
					public LinkedHashSet<String> getQuerySubscriptions() {
						return buildQuerySubscriptions;
					}
					
				};
			}
			
		};
	}
	
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
    
    public static Subject asSubject(@Nullable User user) {
    	if (user != null)
    		return user.asSubject();
    	else
    		return User.asSubject(0L);
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

	public Collection<UserAuthorization> getProjectAuthorizations() {
		return projectAuthorizations;
	}

	public void setProjectAuthorizations(Collection<UserAuthorization> projectAuthorizations) {
		this.projectAuthorizations = projectAuthorizations;
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

	public double getMatchScore(@Nullable String queryTerm) {
		double scoreOfName = MatchScoreUtils.getMatchScore(name, queryTerm);
		double scoreOfFullName = MatchScoreUtils.getMatchScore(fullName, queryTerm);
		return Math.max(scoreOfName, scoreOfFullName);
	}

	public Collection<Group> getGroups() {
		if (groups == null)  
			groups = getMemberships().stream().map(it->it.getGroup()).collect(Collectors.toList());
		return groups;
	}
	
}
