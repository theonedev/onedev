package io.onedev.server.model;

import static io.onedev.server.model.User.PROP_ACCESS_TOKEN;
import static io.onedev.server.model.User.PROP_FULL_NAME;
import static io.onedev.server.model.User.PROP_NAME;
import static io.onedev.server.model.User.PROP_SSO_CONNECTOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.TwoFactorAuthentication;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.validation.annotation.UserName;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Entity
@Table(
		indexes={@Index(columnList=PROP_NAME), @Index(columnList=PROP_FULL_NAME), 
				@Index(columnList=PROP_SSO_CONNECTOR), @Index(columnList=PROP_ACCESS_TOKEN)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class User extends AbstractEntity implements AuthenticationInfo {

	private static final long serialVersionUID = 1L;
	
	public static final int ACCESS_TOKEN_LEN = 40;
	
	public static final Long UNKNOWN_ID = -2L;
	
	public static final Long SYSTEM_ID = -1L;
	
	public static final Long ROOT_ID = 1L;
	
	public static final String SYSTEM_NAME = "OneDev";
	
	public static final String SYSTEM_EMAIL_ADDRESS = "noreply@onedev.io";
	
	public static final String UNKNOWN_NAME = "Unknown";
	
	public static final String EXTERNAL_MANAGED = "external_managed";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_PASSWORD = "password";
	
	public static final String PROP_FULL_NAME = "fullName";
	
	public static final String PROP_SSO_CONNECTOR = "ssoConnector";
	
	public static final String PROP_ACCESS_TOKEN = "accessToken";
	
	private static ThreadLocal<Stack<User>> stack =  new ThreadLocal<Stack<User>>() {

		@Override
		protected Stack<User> initialValue() {
			return new Stack<User>();
		}
	
	};
	
	@Column(unique=true, nullable=false)
    private String name;

    @Column(length=1024, nullable=false)
    @JsonIgnore
    private String password;

	private String fullName;

	@JsonIgnore
	private String ssoConnector;
	
	@Column(unique=true, nullable=false)
	@JsonIgnore
	private String accessToken = RandomStringUtils.randomAlphanumeric(ACCESS_TOKEN_LEN);
	
	@JsonIgnore
	@Lob
	@Column(length=65535)
	private TwoFactorAuthentication twoFactorAuthentication;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> projectAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Dashboard> dashboards = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<DashboardVisit> dashboardVisits = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<DashboardUserShare> dashboardShares = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<IssueAuthorization> issueAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReview> pullRequestReviews = new ArrayList<>();
	
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PullRequestAssignment> pullRequestAssignments = new ArrayList<>();
	
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestWatch> pullRequestWatches = new ArrayList<>();

    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueWatch> issueWatches = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueVote> issueVotes = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueQueryPersonalization> issueQueryPersonalizations = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<BuildQueryPersonalization> buildQueryPersonalizations = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<PullRequestQueryPersonalization> pullRequestQueryPersonalizations = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<CommitQueryPersonalization> commitQueryPersonalizations = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<CodeCommentQueryPersonalization> codeCommentQueryPersonalizations = new ArrayList<>();
    
    @OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Collection<SshKey> sshKeys = new ArrayList<>();
    
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PendingSuggestionApply> pendingSuggestionApplies = new ArrayList<>();

    @OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Collection<EmailAddress> emailAddresses = new ArrayList<>();
    
    @OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Collection<GpgKey> gpgKeys = new ArrayList<>();
    
    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedProjectQuery> projectQueries = new ArrayList<>();
	
    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedIssueQuery> issueQueries = new ArrayList<>();

    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedPullRequestQuery> pullRequestQueries = new ArrayList<>();

    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedBuildQuery> buildQueries = new ArrayList<>();

    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> issueQueryWatches = new LinkedHashMap<>();
	
    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> pullRequestQueryWatches = new LinkedHashMap<>();
	
    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> buildQuerySubscriptions = new LinkedHashSet<>();
	
    private transient Collection<Group> groups;
    
    private transient List<EmailAddress> sortedEmailAddresses;
    
    private transient Optional<EmailAddress> primaryEmailAddress;
    
    private transient Optional<EmailAddress> gitEmailAddress;
    
	public QueryPersonalization<NamedProjectQuery> getProjectQueryPersonalization() {
		return new QueryPersonalization<NamedProjectQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedProjectQuery> getQueries() {
				return projectQueries;
			}

			@Override
			public void setQueries(ArrayList<NamedProjectQuery> userQueries) {
				projectQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedProjectQuery> getQueryWatchSupport() {
				return null;
			}

			@Override
			public QuerySubscriptionSupport<NamedProjectQuery> getQuerySubscriptionSupport() {
				return null;
			}
			
			@Override
			public void onUpdated() {
				OneDev.getInstance(UserManager.class).save(User.this);
			}
			
		};
	}
	
	public QueryPersonalization<NamedIssueQuery> getIssueQueryPersonalization() {
		return new QueryPersonalization<NamedIssueQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedIssueQuery> getQueries() {
				return issueQueries;
			}

			@Override
			public void setQueries(ArrayList<NamedIssueQuery> userQueries) {
				issueQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedIssueQuery> getQueryWatchSupport() {
				return new QueryWatchSupport<NamedIssueQuery>() {

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

			@Override
			public void onUpdated() {
				OneDev.getInstance(UserManager.class).save(User.this);
			}
			
		};
	}
	
	public QueryPersonalization<NamedPullRequestQuery> getPullRequestQueryPersonalization() {
		return new QueryPersonalization<NamedPullRequestQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedPullRequestQuery> getQueries() {
				return pullRequestQueries;
			}

			@Override
			public void setQueries(ArrayList<NamedPullRequestQuery> userQueries) {
				pullRequestQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedPullRequestQuery> getQueryWatchSupport() {
				return new QueryWatchSupport<NamedPullRequestQuery>() {

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

			@Override
			public void onUpdated() {
				OneDev.getInstance(UserManager.class).save(User.this);
			}
			
		};
	}
	
	public QueryPersonalization<NamedBuildQuery> getBuildQueryPersonalization() {
		return new QueryPersonalization<NamedBuildQuery>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedBuildQuery> getQueries() {
				return buildQueries;
			}

			@Override
			public void setQueries(ArrayList<NamedBuildQuery> userQueries) {
				buildQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedBuildQuery> getQueryWatchSupport() {
				return null;
			}

			@Override
			public QuerySubscriptionSupport<NamedBuildQuery> getQuerySubscriptionSupport() {
				return new QuerySubscriptionSupport<NamedBuildQuery>() {

					@Override
					public LinkedHashSet<String> getQuerySubscriptions() {
						return buildQuerySubscriptions;
					}
					
				};
			}

			@Override
			public void onUpdated() {
				OneDev.getInstance(UserManager.class).save(User.this);
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
	@Password(needConfirm=true, autoComplete="new-password")
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

    public boolean isExternalManaged() {
    	return getPassword().equals(EXTERNAL_MANAGED);
    }
    
	@Editable(order=200)
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Nullable
	public String getSsoConnector() {
		return ssoConnector;
	}

	public void setSsoConnector(String ssoConnector) {
		this.ssoConnector = ssoConnector;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Nullable
	public TwoFactorAuthentication getTwoFactorAuthentication() {
		return twoFactorAuthentication;
	}

	public void setTwoFactorAuthentication(TwoFactorAuthentication twoFactorAuthentication) {
		this.twoFactorAuthentication = twoFactorAuthentication;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", getName())
				.toString();
	}
	
	public PersonIdent asPerson() {
		if (isSystem()) {
			return new PersonIdent(User.SYSTEM_NAME, User.SYSTEM_EMAIL_ADDRESS);
		} else {
			EmailAddress emailAddress = getGitEmailAddress();
			if (emailAddress != null && emailAddress.isVerified())
				return new PersonIdent(getDisplayName(), emailAddress.getValue());
			else
		        throw new ExplicitException("No verified email for git operations");
		}
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
	
	public boolean isUnknown() {
		return UNKNOWN_ID.equals(getId());
	}
	
	public boolean isOrdinary() {
		return getId() > 0;
	}
	
	public Collection<UserAuthorization> getProjectAuthorizations() {
		return projectAuthorizations;
	}

	public void setProjectAuthorizations(Collection<UserAuthorization> projectAuthorizations) {
		this.projectAuthorizations = projectAuthorizations;
	}

	public Collection<IssueAuthorization> getIssueAuthorizations() {
		return issueAuthorizations;
	}

	public void setIssueAuthorizations(Collection<IssueAuthorization> issueAuthorizations) {
		this.issueAuthorizations = issueAuthorizations;
	}

	@Override
	public int compareTo(AbstractEntity entity) {
		User user = (User) entity;
		return getDisplayName().compareTo(user.getDisplayName());
	}

	public Collection<Group> getGroups() {
		if (groups == null)  
			groups = getMemberships().stream().map(it->it.getGroup()).collect(Collectors.toList());
		return groups;
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
    
    public Collection<PendingSuggestionApply> getPendingSuggestionApplies() {
		return pendingSuggestionApplies;
	}

	public void setPendingSuggestionApplies(Collection<PendingSuggestionApply> pendingSuggestionApplies) {
		this.pendingSuggestionApplies = pendingSuggestionApplies;
	}

	public Collection<EmailAddress> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Collection<EmailAddress> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public Collection<GpgKey> getGpgKeys() {
		return gpgKeys;
	}

	public void setGpgKeys(Collection<GpgKey> gpgKeys) {
		this.gpgKeys = gpgKeys;
	}

	public boolean isSshKeyExternalManaged() {
    	if (isExternalManaged()) {
    		if (getSsoConnector() != null) {
    			return false;
    		} else {
	    		Authenticator authenticator = OneDev.getInstance(SettingManager.class).getAuthenticator();
	    		return authenticator != null && authenticator.isManagingSshKeys();
    		}
    	} else {
    		return false;
    	}
    }
    
    public boolean isMembershipExternalManaged() {
    	if (isExternalManaged()) {
    		SettingManager settingManager = OneDev.getInstance(SettingManager.class);
    		if (getSsoConnector() != null) {
    			SsoConnector ssoConnector = settingManager.getSsoConnectors().stream()
    					.filter(it->it.getName().equals(getSsoConnector()))
    					.findFirst().orElse(null);
    			return ssoConnector != null && ssoConnector.isManagingMemberships();
    		} else {
	    		Authenticator authenticator = settingManager.getAuthenticator();
	    		return authenticator != null && authenticator.isManagingMemberships();
    		}
    	} else {
    		return false;
    	}
    }

    public String getAuthSource() {
		if (isExternalManaged()) {
			if (getSsoConnector() != null)
				return "SSO Provider: " + getSsoConnector();
			else
				return "External Authenticator";
		} else {
			return "Builtin User Store";
		}
    }

	public Collection<Dashboard> getDashboards() {
		return dashboards;
	}

	public void setDashboards(Collection<Dashboard> dashboards) {
		this.dashboards = dashboards;
	}

	public Collection<DashboardVisit> getDashboardVisits() {
		return dashboardVisits;
	}
	
	public void setDashboardVisits(Collection<DashboardVisit> dashboardVisits) {
		this.dashboardVisits = dashboardVisits;
	}
	
	@Nullable
	public DashboardVisit getDashboardVisit(Dashboard dashboard) {
		for (DashboardVisit visit: getDashboardVisits()) {
			if (visit.getDashboard().equals(dashboard))
				return visit;
		}
		return null;
	}

	public Collection<DashboardUserShare> getDashboardShares() {
		return dashboardShares;
	}

	public void setDashboardShares(Collection<DashboardUserShare> dashboardShares) {
		this.dashboardShares = dashboardShares;
	}

	public Collection<PullRequestReview> getPullRequestReviews() {
		return pullRequestReviews;
	}

	public void setPullRequestReviews(Collection<PullRequestReview> pullRequestReviews) {
		this.pullRequestReviews = pullRequestReviews;
	}

	public Collection<PullRequestAssignment> getPullRequestAssignments() {
		return pullRequestAssignments;
	}

	public void setPullRequestAssignments(Collection<PullRequestAssignment> pullRequestAssignments) {
		this.pullRequestAssignments = pullRequestAssignments;
	}

	public Collection<PullRequestWatch> getPullRequestWatches() {
		return pullRequestWatches;
	}

	public void setPullRequestWatches(Collection<PullRequestWatch> pullRequestWatches) {
		this.pullRequestWatches = pullRequestWatches;
	}

	public Collection<IssueWatch> getIssueWatches() {
		return issueWatches;
	}

	public void setIssueWatches(Collection<IssueWatch> issueWatches) {
		this.issueWatches = issueWatches;
	}

	public Collection<IssueVote> getIssueVotes() {
		return issueVotes;
	}

	public void setIssueVotes(Collection<IssueVote> issueVotes) {
		this.issueVotes = issueVotes;
	}

	public Collection<IssueQueryPersonalization> getIssueQueryPersonalizations() {
		return issueQueryPersonalizations;
	}

	public void setIssueQueryPersonalizations(Collection<IssueQueryPersonalization> issueQueryPersonalizations) {
		this.issueQueryPersonalizations = issueQueryPersonalizations;
	}

	public Collection<BuildQueryPersonalization> getBuildQueryPersonalizations() {
		return buildQueryPersonalizations;
	}

	public void setBuildQueryPersonalizations(Collection<BuildQueryPersonalization> buildQueryPersonalizations) {
		this.buildQueryPersonalizations = buildQueryPersonalizations;
	}

	public Collection<PullRequestQueryPersonalization> getPullRequestQueryPersonalizations() {
		return pullRequestQueryPersonalizations;
	}

	public void setPullRequestQueryPersonalizations(Collection<PullRequestQueryPersonalization> pullRequestQueryPersonalizations) {
		this.pullRequestQueryPersonalizations = pullRequestQueryPersonalizations;
	}

	public Collection<CommitQueryPersonalization> getCommitQueryPersonalizations() {
		return commitQueryPersonalizations;
	}

	public void setCommitQueryPersonalizations(Collection<CommitQueryPersonalization> commitQueryPersonalizations) {
		this.commitQueryPersonalizations = commitQueryPersonalizations;
	}

	public Collection<CodeCommentQueryPersonalization> getCodeCommentQueryPersonalizations() {
		return codeCommentQueryPersonalizations;
	}

	public void setCodeCommentQueryPersonalizations(Collection<CodeCommentQueryPersonalization> codeCommentQueryPersonalizations) {
		this.codeCommentQueryPersonalizations = codeCommentQueryPersonalizations;
	}

	public ArrayList<NamedProjectQuery> getUserProjectQueries() {
		return projectQueries;
	}

	public void setProjectQueries(ArrayList<NamedProjectQuery> userProjectQueries) {
		this.projectQueries = userProjectQueries;
	}

	public ArrayList<NamedIssueQuery> getUserIssueQueries() {
		return issueQueries;
	}

	public void setIssueQueries(ArrayList<NamedIssueQuery> userIssueQueries) {
		this.issueQueries = userIssueQueries;
	}

	public LinkedHashMap<String, Boolean> getIssueQueryWatches() {
		return issueQueryWatches;
	}

	public void setIssueQueryWatches(LinkedHashMap<String, Boolean> issueQueryWatches) {
		this.issueQueryWatches = issueQueryWatches;
	}

	public ArrayList<NamedPullRequestQuery> getUserPullRequestQueries() {
		return pullRequestQueries;
	}

	public void setPullRequestQueries(ArrayList<NamedPullRequestQuery> userPullRequestQueries) {
		this.pullRequestQueries = userPullRequestQueries;
	}

	public LinkedHashMap<String, Boolean> getPullRequestQueryWatches() {
		return pullRequestQueryWatches;
	}

	public void setPullRequestQueryWatches(LinkedHashMap<String, Boolean> pullRequestQueryWatches) {
		this.pullRequestQueryWatches = pullRequestQueryWatches;
	}

	public ArrayList<NamedBuildQuery> getUserBuildQueries() {
		return buildQueries;
	}

	public void setBuildQueries(ArrayList<NamedBuildQuery> buildQueries) {
		this.buildQueries = buildQueries;
	}

	public LinkedHashSet<String> getBuildQuerySubscriptions() {
		return buildQuerySubscriptions;
	}

	public void setBuildQuerySubscriptions(LinkedHashSet<String> buildQuerySubscriptions) {
		this.buildQuerySubscriptions = buildQuerySubscriptions;
	}
	
	public boolean isEnforce2FA() {
		return OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnforce2FA() 
				|| getGroups().stream().anyMatch(it->it.isEnforce2FA());
	}

	public List<EmailAddress> getSortedEmailAddresses() {
		if (sortedEmailAddresses == null) {
			sortedEmailAddresses = new ArrayList<>(getEmailAddresses());
			Collections.sort(sortedEmailAddresses);
		}
		return sortedEmailAddresses;
	}
	
	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
	}
	
	@Nullable
	public EmailAddress getPrimaryEmailAddress() {
		if (primaryEmailAddress == null)
			primaryEmailAddress = Optional.ofNullable(getEmailAddressManager().findPrimary(this));
		return primaryEmailAddress.orElse(null);
	}

	@Nullable
	public EmailAddress getGitEmailAddress() {
		if (gitEmailAddress == null)
			gitEmailAddress = Optional.ofNullable(getEmailAddressManager().findGit(this));
		return gitEmailAddress.orElse(null);
	}
	
	@Override
	public UserFacade getFacade() {
		return new UserFacade(getId(), getName(), getFullName(), getAccessToken());
	}
	
}
