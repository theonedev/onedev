package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.MoreObjects;

import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.build.UserBuildSetting;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.util.match.MatchScoreUtils;
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
	
	public static final Long SYSTEM_ID = -1L;
	
	public static final Long ROOT_ID = 1L;
	
	public static final String EXTERNAL_MANAGED = "external_managed";
	
	private static ThreadLocal<Stack<User>> stack =  new ThreadLocal<Stack<User>>() {

		@Override
		protected Stack<User> initialValue() {
			return new Stack<User>();
		}
	
	};
	
	@Column(unique=true, nullable=false)
    private String name;

    @Column(length=1024, nullable=false)
	@JsonView(DefaultView.class)
    private String password;

	private String fullName;
	
	@Column(unique=true, nullable=false)
	private String email;
	
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
    
    @OneToMany(mappedBy="owner")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Collection<Project> projects = new ArrayList<>();
    
    @OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
    private Collection<SshKey> sshKeys = new ArrayList<>();
    
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedProjectQuery> userProjectQueries = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedIssueQuery> userIssueQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userIssueQueryWatches = new LinkedHashMap<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> issueQueryWatches = new LinkedHashMap<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedPullRequestQuery> userPullRequestQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userPullRequestQueryWatches = new LinkedHashMap<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> pullRequestQueryWatches = new LinkedHashMap<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedBuildQuery> userBuildQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> userBuildQuerySubscriptions = new LinkedHashSet<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> buildQuerySubscriptions = new LinkedHashSet<>();
	
	@Lob
	@Column(length=65535, nullable=false)
	private UserBuildSetting buildSetting = new UserBuildSetting();
	
	@Lob
	@Column(length=65535, nullable=false)
	@JsonView(DefaultView.class)
	private ArrayList<WebHook> webHooks = new ArrayList<>();
	
    private transient Collection<Group> groups;
    
	public QuerySetting<NamedProjectQuery> getProjectQuerySetting() {
		return new QuerySetting<NamedProjectQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedProjectQuery> getUserQueries() {
				return userProjectQueries;
			}

			@Override
			public void setUserQueries(ArrayList<NamedProjectQuery> userQueries) {
				userProjectQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedProjectQuery> getQueryWatchSupport() {
				return null;
			}

			@Override
			public QuerySubscriptionSupport<NamedProjectQuery> getQuerySubscriptionSupport() {
				return null;
			}
			
		};
	}
	
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
    	return SecurityUtils.asSubject(getId());
    }

    public Collection<Project> getProjects() {
		return projects;
	}

	public void setProjects(Collection<Project> projects) {
		this.projects = projects;
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
		if (isSystem())
			return new PersonIdent(getDisplayName(), "");
		else
			return new PersonIdent(getDisplayName(), getEmail());
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

	public boolean isSystem() {
		return SYSTEM_ID.equals(getId());
	}
	
	public Collection<UserAuthorization> getProjectAuthorizations() {
		return projectAuthorizations;
	}

	public void setProjectAuthorizations(Collection<UserAuthorization> projectAuthorizations) {
		this.projectAuthorizations = projectAuthorizations;
	}
	
	public UserBuildSetting getBuildSetting() {
		return buildSetting;
	}

	public void setBuildSetting(UserBuildSetting buildSetting) {
		this.buildSetting = buildSetting;
	}

	public ArrayList<WebHook> getWebHooks() {
		return webHooks;
	}

	public void setWebHooks(ArrayList<WebHook> webHooks) {
		this.webHooks = webHooks;
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
	
	public static User from(@Nullable User user, @Nullable String displayName) {
		if (user == null) {
			user = new User();
			if (displayName != null)
				user.setName(displayName);
			else
				user.setName("Unknown");
		}
		return user;
	}
	
	public static void push(User user) {
		stack.get().push(user);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	@Nullable
	public static User get() {
		if (!stack.get().isEmpty())
			return stack.get().peek();
		else 
			return SecurityUtils.getUser();
	}

    public Collection<SshKey> getSshKeys() {
        return sshKeys;
    }

    public void setSshKeys(Collection<SshKey> sshKeys) {
        this.sshKeys = sshKeys;
    }
	
}
