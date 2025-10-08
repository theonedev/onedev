package io.onedev.server.model;

import static io.onedev.server.model.User.PROP_FULL_NAME;
import static io.onedev.server.model.User.PROP_NAME;
import static io.onedev.server.security.SecurityUtils.asPrincipals;
import static io.onedev.server.security.SecurityUtils.asUserPrincipal;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.SubscriptionRequired;
import io.onedev.server.annotation.UserName;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.TwoFactorAuthentication;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.pack.NamedPackQuery;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;
import io.onedev.server.web.util.WicketUtils;

@Entity
@Table(indexes={@Index(columnList=PROP_NAME), @Index(columnList=PROP_FULL_NAME)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class User extends AbstractEntity implements AuthenticationInfo {

	private static final long serialVersionUID = 1L;
	
	public static final Long UNKNOWN_ID = -2L;
	
	public static final Long SYSTEM_ID = -1L;
	
	public static final Long ROOT_ID = 1L;
	
	public static final String SYSTEM_NAME = "OneDev";
	
	public static final String SYSTEM_EMAIL_ADDRESS = "system@onedev";
	
	public static final String UNKNOWN_NAME = "unknown";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_FULL_NAME = "fullName";
		
	public static final String PROP_SERVICE_ACCOUNT = "serviceAccount";

	public static final String PROP_DISABLED = "disabled";

	public static final String PROP_NOTIFY_OWN_EVENTS = "notifyOwnEvents";

	public static final String PROP_PASSWORD = "password";
	
	private static ThreadLocal<Stack<User>> stack = ThreadLocal.withInitial(() -> new Stack<>());
	
	private boolean serviceAccount;

	private boolean disabled;

	@Column(unique=true, nullable=false)
    private String name;
	
	private String passwordResetCode;

    @Column(length=1024)
    @JsonIgnore
    private String password;

	private String fullName;
			
	private boolean notifyOwnEvents;
	
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
	
	@OneToMany(mappedBy="owner", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<AccessToken> accessTokens = new ArrayList<>();
	
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
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<SsoAccount> ssoAccounts = new ArrayList<>();
	
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
	private Collection<IssueWork> issueWorks = new ArrayList<>();

	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Stopwatch> stopwatches = new ArrayList<>();
	
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<IssueQueryPersonalization> issueQueryPersonalizations = new ArrayList<>();
    
    @OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
    private Collection<BuildQueryPersonalization> buildQueryPersonalizations = new ArrayList<>();

	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE)
	private Collection<PackQueryPersonalization> packQueryPersonalizations = new ArrayList<>();
	
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

	@OneToMany(mappedBy=CodeCommentMention.PROP_USER, cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<CodeCommentMention> codeCommentMentions = new ArrayList<>();

	@OneToMany(mappedBy=IssueMention.PROP_USER, cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<IssueMention> issueMentions = new ArrayList<>();

	@OneToMany(mappedBy=PullRequestMention.PROP_USER, cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<PullRequestMention> pullRequestMentions = new ArrayList<>();
	
	@OneToMany(mappedBy=IssueDescriptionRevision.PROP_USER, cascade=CascadeType.REMOVE)
	private Collection<IssueDescriptionRevision> issueDescriptionRevisions = new ArrayList<>();

	@OneToMany(mappedBy=PullRequestDescriptionRevision.PROP_USER, cascade=CascadeType.REMOVE)
	private Collection<PullRequestDescriptionRevision> pullRequestDescriptionRevisions = new ArrayList<>();

	@OneToMany(mappedBy=IssueCommentRevision.PROP_USER, cascade=CascadeType.REMOVE)
	private Collection<IssueCommentRevision> issueCommentRevisions = new ArrayList<>();

	@OneToMany(mappedBy=PullRequestCommentRevision.PROP_USER, cascade=CascadeType.REMOVE)
	private Collection<PullRequestCommentRevision> pullRequestCommentRevisions = new ArrayList<>();

	@OneToMany(mappedBy=ReviewedDiff.PROP_USER, cascade=CascadeType.REMOVE)
	private Collection<ReviewedDiff> reviewedDiffs = new ArrayList<>();

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
	private ArrayList<NamedPackQuery> packQueries = new ArrayList<>();
	
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

	@JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> packQuerySubscriptions = new LinkedHashSet<>();
	
    private transient Collection<Group> groups;
    
    private transient List<EmailAddress> sortedEmailAddresses;
    
    private transient Optional<EmailAddress> primaryEmailAddress;
    
    private transient Optional<EmailAddress> gitEmailAddress;

    private transient Optional<EmailAddress> publicEmailAddress;
	
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
				OneDev.getInstance(UserService.class).update(User.this, null);
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
				OneDev.getInstance(UserService.class).update(User.this, null);
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
				OneDev.getInstance(UserService.class).update(User.this, null);
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
				OneDev.getInstance(UserService.class).update(User.this, null);
			}
			
		};
	}

	public QueryPersonalization<NamedPackQuery> getPackQueryPersonalization() {
		return new QueryPersonalization<>() {

			@Override
			public Project getProject() {
				return null;
			}

			@Override
			public User getUser() {
				return User.this;
			}

			@Override
			public ArrayList<NamedPackQuery> getQueries() {
				return packQueries;
			}

			@Override
			public void setQueries(ArrayList<NamedPackQuery> userQueries) {
				packQueries = userQueries;
			}

			@Override
			public QueryWatchSupport<NamedPackQuery> getQueryWatchSupport() {
				return null;
			}

			@Override
			public QuerySubscriptionSupport<NamedPackQuery> getQuerySubscriptionSupport() {
				return new QuerySubscriptionSupport<>() {

					@Override
					public LinkedHashSet<String> getQuerySubscriptions() {
						return packQuerySubscriptions;
					}

				};
			}

			@Override
			public void onUpdated() {
				OneDev.getInstance(UserService.class).update(User.this, null);
			}

		};
	}
	
	@Override
    public PrincipalCollection getPrincipals() {
		return asPrincipals(asUserPrincipal(getId()));		
    }
    
    @Override
    public Object getCredentials() {
    	return password!=null?password:"";
    }

    public Subject asSubject() {
    	return SecurityUtils.asSubject(getPrincipals());
    }

	@Editable(order=50, name="Service Account", descriptionProvider = "getServiceAccountDescription")
	@SubscriptionRequired
	public boolean isServiceAccount() {
		return serviceAccount;
	}

	public void setServiceAccount(boolean serviceAccount) {
		this.serviceAccount = serviceAccount;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}			

	@SuppressWarnings("unused")
	private static String getServiceAccountDescription() {
		if (!WicketUtils.isSubscriptionActive()) {
			return _T("" 
				+ "Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate "
				+ "notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. " 
				+ "<a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days");
		} else {
			return _T("" 
				+ "Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate "
				+ "notifications for its activities");
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

	/**
	 * Password will be null if user is created by external authenticator. However, when 
	 * created via OneDev, it is required. So it has @NotEmpty and @Nullable in the same 
	 * time
	 */
	@Editable(order=150)
	@DependsOn(property="serviceAccount", value="false")
	@Password(checkPolicy=true, autoComplete="new-password")
	@NotEmpty
	@Nullable
	public String getPassword() {
		return password;
	}

    /**
     * Set password of this user. 
     * 
     * @param password
     * 			password to set
     */
    public void setPassword(@Nullable String password) {
    	this.password = password;
    }

	public String getPasswordResetCode() {
		return passwordResetCode;
	}

	public void setPasswordResetCode(String passwordResetCode) {
		this.passwordResetCode = passwordResetCode;
	}
    
	@Editable(order=200)
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	@Editable(order=400, name="Notify Own Events", description = "Whether or not to send notifications for events generated by yourself")
	@DependsOn(property="serviceAccount", value="false")
	public boolean isNotifyOwnEvents() {
		return notifyOwnEvents;
	}

	public void setNotifyOwnEvents(boolean sendOwnEvents) {
		this.notifyOwnEvents = sendOwnEvents;
	}

	public Collection<AccessToken> getAccessTokens() {
		return accessTokens;
	}

	public void setAccessTokens(Collection<AccessToken> accessTokens) {
		this.accessTokens = accessTokens;
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

	public Collection<SsoAccount> getSsoAccounts() {
		return ssoAccounts;
	}

	public void setSsoAccounts(Collection<SsoAccount> ssoAccounts) {
		this.ssoAccounts = ssoAccounts;
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
	
    public String getAuthSource() {
		if (getPassword() == null) 
			return "External System";
		else 
			return "Internal Database";
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

	public Collection<IssueWork> getIssueWorks() {
		return issueWorks;
	}

	public void setIssueWorks(Collection<IssueWork> issueWorks) {
		this.issueWorks = issueWorks;
	}

	public Collection<Stopwatch> getStopwatches() {
		return stopwatches;
	}

	public void setStopwatches(Collection<Stopwatch> stopwatches) {
		this.stopwatches = stopwatches;
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

	public Collection<PackQueryPersonalization> getPackQueryPersonalizations() {
		return packQueryPersonalizations;
	}

	public void setPackQueryPersonalizations(Collection<PackQueryPersonalization> packQueryPersonalizations) {
		this.packQueryPersonalizations = packQueryPersonalizations;
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

	public Collection<CodeCommentMention> getCodeCommentMentions() {
		return codeCommentMentions;
	}

	public void setCodeCommentMentions(Collection<CodeCommentMention> codeCommentMentions) {
		this.codeCommentMentions = codeCommentMentions;
	}

	public Collection<IssueMention> getIssueMentions() {
		return issueMentions;
	}

	public void setIssueMentions(Collection<IssueMention> issueMentions) {
		this.issueMentions = issueMentions;
	}

	public Collection<PullRequestMention> getPullRequestMentions() {
		return pullRequestMentions;
	}

	public void setPullRequestMentions(Collection<PullRequestMention> pullRequestMentions) {
		this.pullRequestMentions = pullRequestMentions;
	}

	public ArrayList<NamedProjectQuery> getProjectQueries() {
		return projectQueries;
	}

	public void setProjectQueries(ArrayList<NamedProjectQuery> projectQueries) {
		this.projectQueries = projectQueries;
	}

	@Nullable
	public NamedProjectQuery getProjectQuery(String name) {
		for (var query: getProjectQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}

	public ArrayList<NamedIssueQuery> getIssueQueries() {
		return issueQueries;
	}

	public void setIssueQueries(ArrayList<NamedIssueQuery> issueQueries) {
		this.issueQueries = issueQueries;
	}

	@Nullable
	public NamedIssueQuery getIssueQuery(String name) {
		for (var query: getIssueQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}		

	public LinkedHashMap<String, Boolean> getIssueQueryWatches() {
		return issueQueryWatches;
	}

	public void setIssueQueryWatches(LinkedHashMap<String, Boolean> issueQueryWatches) {
		this.issueQueryWatches = issueQueryWatches;
	}

	public ArrayList<NamedPullRequestQuery> getPullRequestQueries() {
		return pullRequestQueries;
	}

	public void setPullRequestQueries(ArrayList<NamedPullRequestQuery> pullRequestQueries) {
		this.pullRequestQueries = pullRequestQueries;
	}

	@Nullable
	public NamedPullRequestQuery getPullRequestQuery(String name) {
		for (var query: getPullRequestQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}
	
	public LinkedHashMap<String, Boolean> getPullRequestQueryWatches() {
		return pullRequestQueryWatches;
	}

	public void setPullRequestQueryWatches(LinkedHashMap<String, Boolean> pullRequestQueryWatches) {
		this.pullRequestQueryWatches = pullRequestQueryWatches;
	}

	public ArrayList<NamedBuildQuery> getBuildQueries() {
		return buildQueries;
	}

	public void setBuildQueries(ArrayList<NamedBuildQuery> buildQueries) {
		this.buildQueries = buildQueries;
	}

	@Nullable
	public NamedBuildQuery getBuildQuery(String name) {
		for (var query: getBuildQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}
	
	public ArrayList<NamedPackQuery> getPackQueries() {
		return packQueries;
	}

	public void setPackQueries(ArrayList<NamedPackQuery> packQueries) {
		this.packQueries = packQueries;
	}

	@Nullable
	public NamedPackQuery getPackQuery(String name) {
		for (var query: getPackQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}
	
	public LinkedHashSet<String> getBuildQuerySubscriptions() {
		return buildQuerySubscriptions;
	}

	public void setBuildQuerySubscriptions(LinkedHashSet<String> buildQuerySubscriptions) {
		this.buildQuerySubscriptions = buildQuerySubscriptions;
	}

	public LinkedHashSet<String> getPackQuerySubscriptions() {
		return packQuerySubscriptions;
	}

	public void setPackQuerySubscriptions(LinkedHashSet<String> packQuerySubscriptions) {
		this.packQuerySubscriptions = packQuerySubscriptions;
	}

	public boolean isEnforce2FA() {
		return OneDev.getInstance(SettingService.class).getSecuritySetting().isEnforce2FA()
				|| getGroups().stream().anyMatch(it->it.isEnforce2FA());
	}

	public List<EmailAddress> getSortedEmailAddresses() {
		if (sortedEmailAddresses == null) {
			sortedEmailAddresses = new ArrayList<>(getEmailAddresses());
			Collections.sort(sortedEmailAddresses);
		}
		return sortedEmailAddresses;
	}
	
	private EmailAddressService getEmailAddressService() {
		return OneDev.getInstance(EmailAddressService.class);
	}
	
	@Nullable
	public EmailAddress getPrimaryEmailAddress() {
		if (primaryEmailAddress == null)
			primaryEmailAddress = Optional.ofNullable(getEmailAddressService().findPrimary(this));
		return primaryEmailAddress.orElse(null);
	}

	@Nullable
	public EmailAddress getGitEmailAddress() {
		if (gitEmailAddress == null)
			gitEmailAddress = Optional.ofNullable(getEmailAddressService().findGit(this));
		return gitEmailAddress.orElse(null);
	}

	@Nullable
	public EmailAddress getPublicEmailAddress() {
		if (publicEmailAddress == null)
			publicEmailAddress = Optional.ofNullable(getEmailAddressService().findPublic(this));
		return publicEmailAddress.orElse(null);
	}

	public void addEmailAddress(EmailAddress emailAddress) {
		emailAddress.setOwner(this);
		emailAddress.setPrimary(getEmailAddresses().stream().noneMatch(it->it.isPrimary()));		
		emailAddress.setGit(getEmailAddresses().stream().noneMatch(it->it.isGit()));
		getEmailAddresses().add(emailAddress);
	}
	
	public UserFacade getFacade() {
		return new UserFacade(getId(), getName(), getFullName(), isServiceAccount(), isDisabled());
	}
	
}
