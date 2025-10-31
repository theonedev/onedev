package io.onedev.server.model;

import static io.onedev.commons.utils.match.WildcardUtils.matchPath;
import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_PATH;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.commons.lang3.StringUtils.replace;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.Permission;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.ProjectKey;
import io.onedev.server.annotation.ProjectName;
import io.onedev.server.annotation.SubscriptionRequired;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.BuildQueryPersonalizationService;
import io.onedev.server.service.CodeCommentQueryPersonalizationService;
import io.onedev.server.service.CommitQueryPersonalizationService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueQueryPersonalizationService;
import io.onedev.server.service.PackQueryPersonalizationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestQueryPersonalizationService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.LfsObject;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.git.service.CommitMessageError;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.git.signatureverification.SignatureVerificationService;
import io.onedev.server.git.signatureverification.VerificationSuccessful;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.support.CodeAnalysisSetting;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.model.support.build.DefaultFixedIssueFilter;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.model.support.pack.NamedPackQuery;
import io.onedev.server.model.support.pack.ProjectPackSetting;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.search.entity.SortField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.web.UrlService;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.xodus.CommitInfoService;

@Entity
@Table(
		indexes={
				@Index(columnList="o_parent_id"), @Index(columnList="o_forkedFrom_id"),
				@Index(columnList="o_lastActivityDate_id"), @Index(columnList=PROP_NAME), 
				@Index(columnList=PROP_PATH)
		}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_parent_id", PROP_NAME})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate 
@Editable
public class Project extends AbstractEntity implements LabelSupport<ProjectLabel> {

	private static final long serialVersionUID = 1L;
	
	public static final String BUILDS_DIR = "builds";

	public static final String ATTACHMENT_DIR = "attachment";

	public static final String SITE_DIR = "site";
	
	public static final String SHARE_TEST_DIR = ".onedev-share-test";
	
	public static final int MAX_DESCRIPTION_LEN = 15000;
	
	public static final String NAME_NAME = "Name";
	
	public static final String PROP_NAME = "name";

	public static final String NAME_KEY = "Key";
	
	public static final String PROP_KEY = "key";
	
	public static final String NAME_PATH = "Path";
	
	public static final String PROP_PATH = "path";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String PROP_FORKED_FROM = "forkedFrom";
	
	public static final String NAME_LAST_ACTIVITY_DATE = "Last Activity Date";
	
	public static final String PROP_LAST_ACTIVITY_DATE = "lastActivityDate";
	
	public static final String NAME_LABEL = "Label";
	
	public static final String PROP_PARENT = "parent";

	public static final String PROP_PATH_LEN = "pathLen";
	
	public static final String PROP_USER_AUTHORIZATIONS = "userAuthorizations";
	
	public static final String PROP_GROUP_AUTHORIZATIONS = "groupAuthorizations";
	
	public static final String PROP_CODE_MANAGEMENT = "codeManagement";

	public static final String PROP_PACK_MANAGEMENT = "packManagement";
	
	public static final String PROP_ISSUE_MANAGEMENT = "issueManagement";

	public static final String PROP_TIME_TRACKING = "timeTracking";
	
	public static final String NAME_SERVICE_DESK_EMAIL_ADDRESS = "Service Desk Email Address";
	
	public static final String PROP_SERVICE_DESK_EMAIL_ADDRESS = "serviceDeskEmailAddress";
	
	private static final String FAKED_GITHUB_REPO_PATH_SEPARATOR = "-path.separator.of.onedev.project-";

	private static final String FAKED_GITHUB_REPO_OWNER = "faked-owner-of-onedev-project";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_NAME, NAME_KEY, NAME_PATH, NAME_LABEL, NAME_SERVICE_DESK_EMAIL_ADDRESS, NAME_ID, NAME_DESCRIPTION, NAME_LAST_ACTIVITY_DATE);

	public static final Map<String, SortField<Project>> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_PATH, new SortField<>(PROP_PATH));
		SORT_FIELDS.put(NAME_ID, new SortField<>(PROP_ID));
		SORT_FIELDS.put(NAME_NAME, new SortField<>(PROP_NAME));
		SORT_FIELDS.put(NAME_KEY, new SortField<>(PROP_KEY));
		SORT_FIELDS.put(NAME_SERVICE_DESK_EMAIL_ADDRESS, new SortField<>(PROP_SERVICE_DESK_EMAIL_ADDRESS));
		SORT_FIELDS.put(NAME_LAST_ACTIVITY_DATE, new SortField<>(PROP_LAST_ACTIVITY_DATE + "." + ProjectLastActivityDate.PROP_VALUE, DESCENDING));
	}
	
	static ThreadLocal<Stack<Project>> stack = ThreadLocal.withInitial(() -> new Stack<>());
	
	public static void push(Project project) {
		stack.get().push(project);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	private static final ReferenceMap<ObjectId, byte[]> buildSpecBytesCache = 
			new ReferenceMap<>(ReferenceStrength.HARD, ReferenceStrength.SOFT);

	private transient Map<ObjectId, Optional<BuildSpec>> buildSpecCache;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Project forkedFrom;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private Project parent;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(unique=true, nullable=false)
	private ProjectLastActivityDate lastActivityDate;

	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String path;
	
	private int pathLen;

	@Column(unique=true)
	private String key;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	private String description;
	
    @OneToMany(mappedBy="project")
    private Collection<Build> builds = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<JobCache> jobCaches = new ArrayList<>();
	
	@OneToMany(mappedBy= "project")
	private Collection<PackBlob> packBlobs = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Pack> packs = new ArrayList<>();
		
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<TagProtection> tagProtections = new ArrayList<>();

    @Lob
    @Column(nullable=false, length=65535)
	private LinkedHashMap<String, ContributedProjectSetting> contributedSettings = new LinkedHashMap<>();
	
	@Column(nullable=false)
	private Date createDate = new Date();
	
	@OneToMany(mappedBy="targetProject", cascade=CascadeType.REMOVE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceProject")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Issue> issues = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<IssueTouch> issueTouches = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<PullRequestTouch> pullRequestTouches = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentTouch> codeCommentTouches = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<ProjectLabel> labels = new ArrayList<>();
	
    @OneToMany(mappedBy="parent")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> children = new ArrayList<>();
    
    @OneToMany(mappedBy="forkedFrom")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> forks = new ArrayList<>();
    
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<BaseAuthorization> baseAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> groupAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> userAuthorizations = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<AccessTokenAuthorization> accessTokenAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeComment> codeComments = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<IssueQueryPersonalization> issueQueryPersonalizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CommitQueryPersonalization> commitQueryPersonalizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<PullRequestQueryPersonalization> pullRequestQueryPersonalizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentQueryPersonalization> codeCommentQueryPersonalizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<BuildQueryPersonalization> buildQueryPersonalizations = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<PackQueryPersonalization> packQueryPersonalizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Iteration> iterations = new ArrayList<>();

	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Audit> audits = new ArrayList<>();
	
	private boolean codeManagement = true;
	
	private boolean packManagement = true;
	
	private boolean issueManagement = true;

	private boolean timeTracking = false;
		
	@Lob
	@Column(length=65535)
	private GitPackConfig gitPackConfig = new GitPackConfig();
	
	@Lob
	@Column(length=65535)
	private CodeAnalysisSetting codeAnalysisSetting = new CodeAnalysisSetting();
	
	// SQL Server does not allow duplicate null values for unique column. So we use 
	// special prefix to indicate null
	@Column(unique=true)
	private String serviceDeskEmailAddress;
	
	@Lob
	@Column(length=65535, nullable=false)
	private ProjectIssueSetting issueSetting = new ProjectIssueSetting();
	
	@Lob
	@Column(length=65535, nullable=false)
	private ProjectBuildSetting buildSetting = new ProjectBuildSetting();
	
	@Lob
	@Column(length=65535, nullable=false)
	private ProjectPullRequestSetting pullRequestSetting = new ProjectPullRequestSetting();

	@Lob
	@Column(length=65535, nullable=false)
	private ProjectPackSetting packSetting = new ProjectPackSetting();
	
	@Lob
	@Column(length=65535)
	private ArrayList<NamedCommitQuery> namedCommitQueries;
	
	@Lob
	@Column(length=65535)
	private ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries;
	
	@Lob
	@Column(length=65535, nullable=false)
	private ArrayList<WebHook> webHooks = new ArrayList<>();
	
    private transient Map<BlobIdent, Optional<Blob>> blobCache;
    
    private transient Map<String, Optional<ObjectId>> objectIdCache;
    
    private transient Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatusCache;
    
    private transient Map<ObjectId, Optional<RevCommit>> commitCache;
    
    private transient Map<String, Optional<RefFacade>> refCache;
    
    private transient Optional<String> defaultBranch;
    
    private transient Optional<IssueQueryPersonalization> issueQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<PullRequestQueryPersonalization> pullRequestQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<CodeCommentQueryPersonalization> codeCommentQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<BuildQueryPersonalization> buildQueryPersonalizationOfCurrentUserHolder;

	private transient Optional<PackQueryPersonalization> packQueryPersonalizationOfCurrentUserHolder;
	
    private transient Optional<CommitQueryPersonalization> commitQueryPersonalizationOfCurrentUserHolder;
    
	private transient List<Iteration> sortedHierarchyIterations;
	
	private transient Optional<String> activeServer;

	private transient Map<ObjectId, Collection<Build>> buildsCache;
	
	private transient Map<ObjectId, Collection<String>> reachableBranchesCache;
	
	@Editable(order=100)
	@ProjectName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=150, name = "Project Key", description = "Optionally define a unique key for the project with two or " +
			"more upper case letters. This key can be used to reference issues, builds, and pull requests " +
			"with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of " +
			"<code>&lt;project path&gt;#&lt;number&gt;</code>")
	@ProjectKey
	@Nullable
	public String getKey() {
		return key;
	}
	
	public void setKey(@Nullable String key) {
		this.key = key;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		pathLen = path.length();
	}

	public int getPathLen() {
		return pathLen;
	}

	public void setPathLen(int pathLen) {
		this.pathLen = pathLen;
	}

	@Editable(order=200)
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LEN);
	}

	public ArrayList<BranchProtection> getBranchProtections() {
		return branchProtections;
	}

	public List<BranchProtection> getHierarchyBranchProtections() {
		List<BranchProtection> branchProtections = new ArrayList<>(getBranchProtections());
		if (getParent() != null)
			branchProtections.addAll(getParent().getHierarchyBranchProtections());
		return branchProtections;
	}
	
	public void setBranchProtections(ArrayList<BranchProtection> branchProtections) {
		this.branchProtections = branchProtections;
	}

	public ArrayList<TagProtection> getTagProtections() {
		return tagProtections;
	}

	public List<TagProtection> getHierarchyTagProtections() {
		List<TagProtection> tagProtections = new ArrayList<>(getTagProtections());
		if (getParent() != null)
			tagProtections.addAll(getParent().getHierarchyTagProtections());
		return tagProtections;
	}
	
	public void setTagProtections(ArrayList<TagProtection> tagProtections) {
		this.tagProtections = tagProtections;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public ProjectLastActivityDate getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(ProjectLastActivityDate lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public Collection<PullRequest> getIncomingRequests() {
		return incomingRequests;
	}

	public void setIncomingRequests(Collection<PullRequest> incomingRequests) {
		this.incomingRequests = incomingRequests;
	}

	public Collection<PullRequest> getOutgoingRequests() {
		return outgoingRequests;
	}

	public void setOutgoingRequests(Collection<PullRequest> outgoingRequests) {
		this.outgoingRequests = outgoingRequests;
	}

	public Collection<GroupAuthorization> getGroupAuthorizations() {
		return groupAuthorizations;
	}

	public void setGroupAuthorizations(Collection<GroupAuthorization> groupAuthorizations) {
		this.groupAuthorizations = groupAuthorizations;
	}

	public Collection<BaseAuthorization> getBaseAuthorizations() {
		return baseAuthorizations;
	}

	public void setBaseAuthorizations(Collection<BaseAuthorization> baseAuthorizations) {
		this.baseAuthorizations = baseAuthorizations;
	}

	public Collection<UserAuthorization> getUserAuthorizations() {
		return userAuthorizations;
	}

	public void setUserAuthorizations(Collection<UserAuthorization> userAuthorizations) {
		this.userAuthorizations = userAuthorizations;
	}

	@Nullable
	public Project getForkedFrom() {
		return forkedFrom;
	}

	public void setForkedFrom(Project forkedFrom) {
		this.forkedFrom = forkedFrom;
	}

	public Collection<ProjectLabel> getLabels() {
		return labels;
	}

	public void setLabels(Collection<ProjectLabel> labels) {
		this.labels = labels;
	}

	@Nullable
	public Project getParent() {
		return parent;
	}

	public void setParent(Project parent) {
		this.parent = parent;
	}

	public Collection<Project> getChildren() {
		return children;
	}
	
	public void setChildren(Collection<Project> children) {
		this.children = children;
	}

	public List<Project> getDescendants() {
		List<Project> descendants = new ArrayList<>(getChildren());
		for (Project child: getChildren())
			descendants.addAll(child.getDescendants());
		return descendants;
	}

	public List<Project> getAncestors() {
		List<Project> ancestors = new ArrayList<>();
		if (getParent() != null) {
			ancestors.add(getParent());
			ancestors.addAll(getParent().getAncestors());
		} 
		return ancestors;
	}
	
	public Collection<Project> getForks() {
		return forks;
	}

	public void setForks(Collection<Project> forks) {
		this.forks = forks;
	}
	
	public List<RefFacade> getBranchRefs() {
		List<RefFacade> refs = getCommitRefs(Constants.R_HEADS);
		for (Iterator<RefFacade> it = refs.iterator(); it.hasNext();) {
			RefFacade ref = it.next();
			if (ref.getName().equals(GitUtils.branch2ref(getDefaultBranch()))) {
				it.remove();
				refs.add(0, ref);
				break;
			}
		}
		
		return refs;
    }
	
	public List<RefFacade> getTagRefs() {
		return getCommitRefs(Constants.R_TAGS);
    }
	
	public List<RefFacade> getCommitRefs(String prefix) {
		return getGitService().getCommitRefs(this, prefix);
    }

	/**
	 * Find fork root of this project. 
	 * 
	 * @return
	 * 			fork root of this project
	 */
	public Project getForkRoot() {
		if (forkedFrom != null) 
			return forkedFrom.getForkRoot();
		else 
			return this;
	}
	
	/**
	 * Get all descendant projects forking from current project.
	 * 
	 * @return
	 * 			all descendant projects forking from current project
	 */
	public List<Project> getForkDescendants() {
		List<Project> forkDescendants = new ArrayList<>();
		for (Project fork: getForks()) {  
			forkDescendants.add(fork);
			forkDescendants.addAll(fork.getForkDescendants());
		}
		
		return forkDescendants;
	}
	
	public List<Project> getForkAncestors() {
		List<Project> forkAncestors = new ArrayList<>();
		if (getForkedFrom() != null) {
			forkAncestors.add(getForkedFrom());
			forkAncestors.addAll(getForkedFrom().getForkAncestors());
		}
		return forkAncestors;
	}

	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(this, true);
	}
	
	@Nullable
	public String getDefaultBranch() {
		if (defaultBranch == null) 
			defaultBranch = Optional.fromNullable(getGitService().getDefaultBranch(this));
		return defaultBranch.orNull();
	}
	
	public void setDefaultBranch(String defaultBranch) {
		getGitService().setDefaultBranch(this, defaultBranch);
		this.defaultBranch = Optional.of(defaultBranch);
	}
	
	private Map<BlobIdent, Optional<Blob>> getBlobCache() {
		if (blobCache == null) {
			synchronized(this) {
				if (blobCache == null)
					blobCache = new ConcurrentHashMap<>();
			}
		}
		return blobCache;
	}
	
	public ProjectFacade getFacade() {
		return new ProjectFacade(getId(), getName(), getKey(), getPath(), getServiceDeskEmailAddress(), 
				isCodeManagement(), isIssueManagement(), getGitPackConfig(), lastActivityDate.getId(), 
				idOf(getParent()), idOf(getForkedFrom()));
	}
	
	/**
	 * Read blob content and cache result in repository in case the same blob 
	 * content is requested again. 
	 * 
	 * @param blobIdent
	 * 			ident of the blob
	 * @return
	 * 			blob of specified blob ident
	 * @throws
	 * 			ObjectNotFoundException if blob of specified ident can not be found in repository 
	 * 			
	 */
	@Nullable
	public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
		Preconditions.checkArgument(blobIdent.revision!=null && blobIdent.path!=null && blobIdent.mode!=null, 
				"Revision, path and mode of ident param should be specified");
		
		Optional<Blob> blobOptional = getBlobCache().get(blobIdent);
		if (blobOptional == null) {
			ObjectId revId = getObjectId(blobIdent.revision, mustExist);		
			if (revId != null) { 
				Blob blob = getGitService().getBlob(this, revId, blobIdent.path);
				if (blob != null)
					blob = new Blob(blobIdent, blob.getBlobId(), blob.getBytes(), blob.getSize());
				blobOptional = Optional.fromNullable(blob);
			} else {
				blobOptional = Optional.absent();
			}
			getBlobCache().put(blobIdent, blobOptional);
		}
		if (mustExist && !blobOptional.isPresent())
			throw new ObjectNotFoundException("Unable to find blob ident: " + blobIdent);
		else 
			return blobOptional.orNull();
	}
	
	public BlobIdent findBlobIdent(ObjectId revId, String path) {
		return getGitService().getBlobIdent(this, revId, path);
	}
	
	/**
	 * Get cached object id of specified revision.
	 * 
	 * @param revision
	 * 			revision to resolve object id for
	 * @param mustExist
	 * 			true to have the method throwing exception instead 
	 * 			of returning null if the revision does not exist
	 * @return
	 * 			object id of specified revision, or <tt>null</tt> if revision 
	 * 			does not exist and mustExist is specified as false
	 */
	@Nullable
	public ObjectId getObjectId(String revision, boolean mustExist) {
		if (objectIdCache == null)
			objectIdCache = new HashMap<>();
		
		Optional<ObjectId> optional = objectIdCache.get(revision);
		if (optional == null) {
			optional = Optional.fromNullable(getGitService().resolve(this, revision, false));
			objectIdCache.put(revision, optional);
		}
		if (mustExist && !optional.isPresent())
			throw new ObjectNotFoundException("Unable to find revision '" + revision + "'");
		return optional.orNull();
	}
	
	public void cacheObjectId(String revision, @Nullable ObjectId objectId) {
		if (objectIdCache == null)
			objectIdCache = new HashMap<>();
		
		objectIdCache.put(revision, Optional.fromNullable(objectId));
	}

	public Map<String, Status> getCommitStatuses(ObjectId commitId, @Nullable PullRequest request, 
												 @Nullable String refName) {
		Map<String, Collection<StatusInfo>> commitStatusInfos = getCommitStatusCache().get(commitId);
		if (commitStatusInfos == null) {
			BuildService buildService = OneDev.getInstance(BuildService.class);
			commitStatusInfos = buildService.queryStatus(this, Sets.newHashSet(commitId)).get(commitId);
			getCommitStatusCache().put(commitId, Preconditions.checkNotNull(commitStatusInfos));
		}
		Map<String, Status> commitStatuses = new HashMap<>();
		for (Map.Entry<String, Collection<StatusInfo>> entry: commitStatusInfos.entrySet()) {
			Collection<Status> statuses = new ArrayList<>();
			for (StatusInfo statusInfo: entry.getValue()) {
				if ((refName == null || refName.equals(statusInfo.getRefName())) 
						&& Objects.equals(idOf(request), statusInfo.getRequestId())) {
					statuses.add(statusInfo.getStatus());
				}
			}
			commitStatuses.put(entry.getKey(), Status.getOverallStatus(statuses));
		}
		return commitStatuses;
	}
	
	private Map<ObjectId, Map<String, Collection<StatusInfo>>> getCommitStatusCache() {
		if (commitStatusCache == null)
			commitStatusCache = new HashMap<>();
		return commitStatusCache;
	}
	
	public void cacheCommitStatuses(Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatuses) {
		getCommitStatusCache().putAll(commitStatuses);
	}
	
	/**
	 * Get build spec of specified commit
	 * @param commitId
	 * 			commit id to get build spec for 
	 * @return
	 * 			build spec of specified commit, or <tt>null</tt> if build spec is not defined
	 * @throws
	 * 			Exception when build spec is defined but not valid
	 */
	@Nullable
	private BuildSpec loadBuildSpec(ObjectId commitId) {
		byte[] buildSpecBytes;
		synchronized (buildSpecBytesCache) {
			buildSpecBytes = buildSpecBytesCache.get(commitId);
		}
		if (buildSpecBytes == null) {
			Blob blob = getBlob(new BlobIdent(commitId.name(), BuildSpec.BLOB_PATH, FileMode.TYPE_FILE), false);
			BuildSpec buildSpec;
			if (blob != null) {  
				buildSpec = BuildSpec.parse(blob.getBytes());
			} else { 
				Blob oldBlob = getBlob(new BlobIdent(commitId.name(), ".onedev-buildspec", FileMode.TYPE_FILE), false);
				if (oldBlob != null)
					buildSpec = BuildSpec.parse(oldBlob.getBytes());
				else
					buildSpec = null;
			}
			if (buildSpec != null) {
				buildSpecBytes = SerializationUtils.serialize(buildSpec);
				synchronized (buildSpecBytesCache) {
					buildSpecBytesCache.put(commitId, buildSpecBytes);
				}
			}
			return buildSpec;
		} else {
			return SerializationUtils.deserialize(buildSpecBytes);
		}
	}
	
	@Nullable
	public BuildSpec getBuildSpec(ObjectId commitId) {
		Project.push(this);
		try {
			if (buildSpecCache == null)
				buildSpecCache = new HashMap<>();
			Optional<BuildSpec> buildSpec = buildSpecCache.get(commitId);
			if (buildSpec == null) {
				buildSpec = Optional.fromNullable(loadBuildSpec(commitId));
				buildSpecCache.put(commitId, buildSpec);
			}
			return buildSpec.orNull();
		} finally {
			Project.pop();
		}
	}
	
	public List<String> getJobNames() {
		List<String> jobNames = new ArrayList<>();
		if (getDefaultBranch() != null) {
			BuildSpec buildSpec = getBuildSpec(getObjectId(getDefaultBranch(), true));
			if (buildSpec != null)
				jobNames.addAll(buildSpec.getJobMap().keySet());
		}
		return jobNames;
	}
	
	public LastCommitsOfChildren getLastCommitsOfChildren(String revision, @Nullable String path) {
		ObjectId revId = getObjectId(revision, true);
		return getGitService().getLastCommitsOfChildren(this, revId, path);
	}

	@Nullable
	public RefFacade getRef(String revision) {
		if (refCache == null)
			refCache = new HashMap<>();
		Optional<RefFacade> ref = refCache.get(revision);
		if (ref == null) {
			ref = Optional.fromNullable(getGitService().getRef(this, revision));
			refCache.put(revision, ref);
		}
		return ref.orNull();
	}
	
	@Nullable
	public String getRefName(String revision) {
		RefFacade ref = getRef(revision);
		return ref != null? ref.getName(): null;
	}
	
	@Nullable
	public RefFacade getBranchRef(@Nullable String revision) {
		if (revision == null)
			return null;
		if (!revision.startsWith(Constants.R_HEADS))
			revision = GitUtils.branch2ref(revision);
		return getRef(revision);
	}
	
	@Nullable
	public RefFacade getTagRef(@Nullable String revision) {
		if (revision == null)
			return null;
		if (!revision.startsWith(Constants.R_TAGS))
			revision = GitUtils.tag2ref(revision);
		return getRef(revision);
	}
	
	@Nullable
	public RevCommit getRevCommit(String revision, boolean mustExist) {
		ObjectId revId = getObjectId(revision, mustExist);
		if (revId != null) {
			return getRevCommit(revId, mustExist);
		} else {
			return null;
		}
	}
	
	@Nullable
	public RevCommit getRevCommit(ObjectId revId, boolean mustExist) {
		if (commitCache == null)
			commitCache = new HashMap<>();
		Optional<RevCommit> commit = commitCache.get(revId);
		if (commit == null) {
			commit = Optional.fromNullable(getGitService().getCommit(this, revId));
			commitCache.put(revId, commit);
		}
		if (mustExist && !commit.isPresent())
			throw new ObjectNotFoundException("Unable to find commit associated with object id: " + revId);
		else
			return commit.orNull();
	}
	
	public Collection<CodeComment> getCodeComments() {
		return codeComments;
	}

	public void setCodeComments(Collection<CodeComment> codeComments) {
		this.codeComments = codeComments;
	}
	
	@Editable(order=250, description="Whether or not to enable code management for the project")
	public boolean isCodeManagement() {
		return codeManagement;
	}

	public void setCodeManagement(boolean codeManagement) {
		this.codeManagement = codeManagement;
	}

	@Editable(order=300, description="Whether or not to enable issue management for the project")
	public boolean isIssueManagement() {
		return issueManagement;
	}
	
	public void setIssueManagement(boolean issueManagement) {
		this.issueManagement = issueManagement;
	}

	@Editable(order=350, descriptionProvider = "getTimeTrackingDescription")
	@DependsOn(property="issueManagement")
	@SubscriptionRequired
	public boolean isTimeTracking() {
		return timeTracking;
	}

	public void setTimeTracking(boolean timeTracking) {
		this.timeTracking = timeTracking;
	}
	
	@SuppressWarnings("unused")
	private static String getTimeTrackingDescription() {
		if (!WicketUtils.isSubscriptionActive()) {
			return _T("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days");
		} else {
			return _T("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets");
		}
	}

	@Editable(order=400, name="Package Management", descriptionProvider = "getPackManagementDescription")
	public boolean isPackManagement() {
		return packManagement;
	}

	@SuppressWarnings("unused")
	private static String getPackManagementDescription() {
		return _T("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project");
	}

	public void setPackManagement(boolean packManagement) {
		this.packManagement = packManagement;
	}
	
	@Editable(order=500, placeholder="Default", description="Specify an email address sharing same inbox as " +
			"the system email address in mail setting definition. Emails sent to this address will be " +
			"created as issues in this project. The default value takes form of "
			+ "<tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>")
	@Email
	@Pattern(regexp = "[^~]+@.+", message = "character '~' not allowed in name part")
	public String getServiceDeskEmailAddress() {
		return serviceDeskEmailAddress;
	}

	public void setServiceDeskEmailAddress(String serviceDeskEmailAddress) {
		this.serviceDeskEmailAddress = serviceDeskEmailAddress;
	}
	
	public GitPackConfig getGitPackConfig() {
		return gitPackConfig;
	}

	public void setGitPackConfig(GitPackConfig gitPackConfig) {
		this.gitPackConfig = gitPackConfig;
	}

	public CodeAnalysisSetting getCodeAnalysisSetting() {
		return codeAnalysisSetting;
	}

	public void setCodeAnalysisSetting(CodeAnalysisSetting codeAnalysisSetting) {
		this.codeAnalysisSetting = codeAnalysisSetting;
	}

	public ProjectIssueSetting getIssueSetting() {
		return issueSetting;
	}

	public void setIssueSetting(ProjectIssueSetting issueSetting) {
		this.issueSetting = issueSetting;
	}

	public ProjectBuildSetting getBuildSetting() {
		return buildSetting;
	}

	public void setBuildSetting(ProjectBuildSetting buildSetting) {
		this.buildSetting = buildSetting;
	}

	public ProjectPackSetting getPackSetting() {
		return packSetting;
	}

	public void setPackSetting(ProjectPackSetting packSetting) {
		this.packSetting = packSetting;
	}

	public List<JobSecret> getHierarchyJobSecrets() {
		List<JobSecret> jobSecrets = new ArrayList<>(getBuildSetting().getJobSecrets());
		if (getParent() != null) 
			jobSecrets.addAll(getParent().getHierarchyJobSecrets());
		return jobSecrets;
	}

	public List<JobProperty> getHierarchyJobProperties() {
		List<JobProperty> jobProperties = new ArrayList<>(getBuildSetting().getJobProperties());
		if (getParent() != null) {
			Set<String> names = jobProperties.stream().map(it->it.getName()).collect(Collectors.toSet());
			for (JobProperty jobProperty : getParent().getHierarchyJobProperties()) {
				if (!names.contains(jobProperty.getName()))
					jobProperties.add(jobProperty);
			}
		}
		return jobProperties;
	}
	
	public List<DefaultFixedIssueFilter> getHierarchyDefaultFixedIssueFilters() {
		List<DefaultFixedIssueFilter> defaultFixedIssueFilters = new ArrayList<>(getBuildSetting().getDefaultFixedIssueFilters());
		if (getParent() != null)
			defaultFixedIssueFilters.addAll(getParent().getHierarchyDefaultFixedIssueFilters());
		return defaultFixedIssueFilters;
	}
	
	public List<BuildPreservation> getHierarchyBuildPreservations() {
		List<BuildPreservation> buildPreservations = new ArrayList<>(getBuildSetting().getBuildPreservations());
		if (getParent() != null)
			buildPreservations.addAll(getParent().getHierarchyBuildPreservations());
		return buildPreservations;
	}
	
	@Nullable
	public String getHierarchyDefaultFixedIssueQuery(String jobName) {
		Matcher matcher = new StringMatcher();
		for (DefaultFixedIssueFilter each: getHierarchyDefaultFixedIssueFilters()) {
			if (PatternSet.parse(each.getJobNames()).matches(matcher, jobName))
				return each.getIssueQuery();
		}
		return null;
	}
	
	public int getHierarchyCachePreserveDays() {
		var cachePreserveDays = getBuildSetting().getCachePreserveDays();
		if (cachePreserveDays != null)
			return cachePreserveDays;
		else if (getParent() != null)
			return getParent().getHierarchyCachePreserveDays();
		else 
			return ProjectBuildSetting.DEFAULT_CACHE_PRESERVE_DAYS; 
	}
	
	public ProjectPullRequestSetting getPullRequestSetting() {
		return pullRequestSetting;
	}

	public void setPullRequestSetting(ProjectPullRequestSetting pullRequestSetting) {
		this.pullRequestSetting = pullRequestSetting;
	}
	
	public ArrayList<NamedCommitQuery> getNamedCommitQueries() {
		if (namedCommitQueries == null) {
			namedCommitQueries = new ArrayList<>();
			namedCommitQueries.add(new NamedCommitQuery("All", null));
			namedCommitQueries.add(new NamedCommitQuery("Default branch", "default-branch"));
			namedCommitQueries.add(new NamedCommitQuery("Authored by me", "authored-by-me"));
			namedCommitQueries.add(new NamedCommitQuery("Committed by me", "committed-by-me"));
			namedCommitQueries.add(new NamedCommitQuery("Committed recently", "after(last week)"));
		}
		return namedCommitQueries;
	}

	public void setNamedCommitQueries(ArrayList<NamedCommitQuery> namedCommitQueries) {
		this.namedCommitQueries = namedCommitQueries;
	}

	public ArrayList<NamedCodeCommentQuery> getNamedCodeCommentQueries() {
		if (namedCodeCommentQueries == null) {
			namedCodeCommentQueries = new ArrayList<>(); 
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Unresolved", "unresolved"));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Created by me", "created by me"));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Mentioned me", "mentioned me"));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Created recently", "\"Create Date\" is since \"last week\""));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Has activity recently", "\"Last Activity Date\" is since \"last week\""));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Resolved", "resolved"));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("All", null));
		}
		return namedCodeCommentQueries;
	}

	public void setNamedCodeCommentQueries(ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries) {
		this.namedCodeCommentQueries = namedCodeCommentQueries;
	}
	
	public Collection<IssueQueryPersonalization> getIssueQueryPersonalizations() {
		return issueQueryPersonalizations;
	}

	public void setIssueQueryPersonalizations(Collection<IssueQueryPersonalization> issueQueryPersonalizations) {
		this.issueQueryPersonalizations = issueQueryPersonalizations;
	}

	public Collection<CommitQueryPersonalization> getCommitQueryPersonalizations() {
		return commitQueryPersonalizations;
	}

	public void setCommitQueryPersonalizations(Collection<CommitQueryPersonalization> commitQueryPersonalizations) {
		this.commitQueryPersonalizations = commitQueryPersonalizations;
	}

	public Collection<PullRequestQueryPersonalization> getPullRequestQueryPersonalizations() {
		return pullRequestQueryPersonalizations;
	}

	public void setPullRequestQueryPersonalizations(Collection<PullRequestQueryPersonalization> pullRequestQueryPersonalizations) {
		this.pullRequestQueryPersonalizations = pullRequestQueryPersonalizations;
	}

	public Collection<CodeCommentQueryPersonalization> getCodeCommentQueryPersonalizations() {
		return codeCommentQueryPersonalizations;
	}

	public void setCodeCommentQueryPersonalizations(Collection<CodeCommentQueryPersonalization> codeCommentQueryPersonalizations) {
		this.codeCommentQueryPersonalizations = codeCommentQueryPersonalizations;
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

	public Collection<Build> getBuilds() {
		return builds;
	}

	public void setBuilds(Collection<Build> builds) {
		this.builds = builds;
	}

	public Collection<JobCache> getJobCaches() {
		return jobCaches;
	}

	public void setJobCaches(Collection<JobCache> jobCaches) {
		this.jobCaches = jobCaches;
	}

	public Collection<PackBlob> getPackBlobs() {
		return packBlobs;
	}

	public void setPackBlobs(Collection<PackBlob> packBlobs) {
		this.packBlobs = packBlobs;
	}

	public Collection<Pack> getPacks() {
		return packs;
	}

	public void setPacks(Collection<Pack> packs) {
		this.packs = packs;
	}

	public List<BlobIdent> getBlobChildren(BlobIdent blobIdent, BlobIdentFilter filter) {
		return getBlobChildren(blobIdent, filter, getObjectId(blobIdent.revision, true));
	}
	
	public List<BlobIdent> getBlobChildren(BlobIdent blobIdent, BlobIdentFilter filter, 
			ObjectId commitId) {
		List<BlobIdent> children = getGitService().getChildren(
				this, commitId, blobIdent.path, filter, false);
		for (BlobIdent child: children)
			child.revision = blobIdent.revision;
		return children;
	}

	public int getMode(String revision, String path) {
		return getGitService().getMode(this, getObjectId(revision, true), path);
	}

	public Collection<Iteration> getIterations() {
		return iterations;
	}
	
	public Collection<Iteration> getHierarchyIterations() {
		Collection<Iteration> iterations = new ArrayList<>(getIterations());
		if (getParent() != null)
			iterations.addAll(getParent().getHierarchyIterations());
		return iterations;
	}

	public void setIterations(Collection<Iteration> iterations) {
		this.iterations = iterations;
	}

	public List<Iteration> getSortedHierarchyIterations() {
		if (sortedHierarchyIterations == null) {
			sortedHierarchyIterations = new ArrayList<>(getHierarchyIterations());
			sortedHierarchyIterations.sort(new Iteration.DatesAndStatusComparator());
		}
		return sortedHierarchyIterations;
	}
	
	@Editable
	public ArrayList<WebHook> getWebHooks() {
		return webHooks;
	}

	public List<WebHook> getHierarchyWebHooks() {
		List<WebHook> webHooks = new ArrayList<>(getWebHooks());
		if (getParent() != null)
			webHooks.addAll(getParent().getHierarchyWebHooks());
		return webHooks;
	}
	
	public void setWebHooks(ArrayList<WebHook> webHooks) {
		this.webHooks = webHooks;
	}

	public TagProtection getTagProtection(String tagName, User user) {
		for (TagProtection protection: getHierarchyTagProtections()) {
			if (protection.isEnabled() 
					&& UserMatch.parse(protection.getUserMatch()).matches(user) 
					&& PatternSet.parse(protection.getTags()).matches(new PathMatcher(), tagName)) {
				return protection;
			}
		}
		
		TagProtection protection = new TagProtection();
		protection.setPreventCreation(false);
		protection.setPreventDeletion(false);
		protection.setPreventUpdate(false);
		
		return protection;
	}
	
	public BranchProtection getBranchProtection(@Nullable String branchName, @Nullable User user) {
		if (branchName == null)
			branchName = "main";
		for (BranchProtection protection: getHierarchyBranchProtections()) {
			if (protection.isEnabled() 
					&& UserMatch.parse(protection.getUserMatch()).matches(user)
					&& PatternSet.parse(protection.getBranches()).matches(new PathMatcher(), branchName)) {
				return protection;
			}
		}
		
		BranchProtection protection = new BranchProtection();
		protection.setPreventCreation(false);
		protection.setPreventDeletion(false);
		protection.setPreventForcedPush(false);
		
		return protection;
	}

	@Override
	public String toString() {
		return getPath();
	}

	public List<User> getAuthors(String filePath, ObjectId commitId, @Nullable LinearRange range) {
		List<User> authors = new ArrayList<>();
		EmailAddressService emailAddressService = OneDev.getInstance(EmailAddressService.class);
		for (BlameBlock block: getGitService().blame(this, commitId, filePath, range)) {
			EmailAddress emailAddress = emailAddressService.findByPersonIdent(block.getCommit().getAuthor());
			if (emailAddress != null && emailAddress.isVerified() && !authors.contains(emailAddress.getOwner()))
				authors.add(emailAddress.getOwner());
		}
		
		return authors;
	}
	
	public IssueQueryPersonalization getIssueQueryPersonalizationOfCurrentUser() {
		if (issueQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getAuthUser();
			if (user != null) {
				IssueQueryPersonalization personalization = 
						OneDev.getInstance(IssueQueryPersonalizationService.class).find(this, user);
				if (personalization == null) {
					personalization = new IssueQueryPersonalization();
					personalization.setProject(this);
					personalization.setUser(user);
				}
				issueQueryPersonalizationOfCurrentUserHolder = Optional.of(personalization);
			} else {
				issueQueryPersonalizationOfCurrentUserHolder = Optional.absent();
			}
		}
		return issueQueryPersonalizationOfCurrentUserHolder.orNull();
	}
	
	public CommitQueryPersonalization getCommitQueryPersonalizationOfCurrentUser() {
		if (commitQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getAuthUser();
			if (user != null) {
				CommitQueryPersonalization personalization = 
						OneDev.getInstance(CommitQueryPersonalizationService.class).find(this, user);
				if (personalization == null) {
					personalization = new CommitQueryPersonalization();
					personalization.setProject(this);
					personalization.setUser(user);
				}
				commitQueryPersonalizationOfCurrentUserHolder = Optional.of(personalization);
			} else {
				commitQueryPersonalizationOfCurrentUserHolder = Optional.absent();
			}
		}
		return commitQueryPersonalizationOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public PullRequestQueryPersonalization getPullRequestQueryPersonalizationOfCurrentUser() {
		if (pullRequestQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getAuthUser();
			if (user != null) {
				PullRequestQueryPersonalization personalization = 
						OneDev.getInstance(PullRequestQueryPersonalizationService.class).find(this, user);
				if (personalization == null) {
					personalization = new PullRequestQueryPersonalization();
					personalization.setProject(this);
					personalization.setUser(user);
				}
				pullRequestQueryPersonalizationOfCurrentUserHolder = Optional.of(personalization);
			} else {
				pullRequestQueryPersonalizationOfCurrentUserHolder = Optional.absent();
			}
		}
		return pullRequestQueryPersonalizationOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public CodeCommentQueryPersonalization getCodeCommentQueryPersonalizationOfCurrentUser() {
		if (codeCommentQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getAuthUser();
			if (user != null) {
				CodeCommentQueryPersonalization personalization = 
						OneDev.getInstance(CodeCommentQueryPersonalizationService.class).find(this, user);
				if (personalization == null) {
					personalization = new CodeCommentQueryPersonalization();
					personalization.setProject(this);
					personalization.setUser(user);
				}
				codeCommentQueryPersonalizationOfCurrentUserHolder = Optional.of(personalization);
			} else {
				codeCommentQueryPersonalizationOfCurrentUserHolder = Optional.absent();
			}
		}
		return codeCommentQueryPersonalizationOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public BuildQueryPersonalization getBuildQueryPersonalizationOfCurrentUser() {
		if (buildQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getAuthUser();
			if (user != null) {
				BuildQueryPersonalization personalization = 
						OneDev.getInstance(BuildQueryPersonalizationService.class).find(this, user);
				if (personalization == null) {
					personalization = new BuildQueryPersonalization();
					personalization.setProject(this);
					personalization.setUser(user);
				}
				buildQueryPersonalizationOfCurrentUserHolder = Optional.of(personalization);
			} else {
				buildQueryPersonalizationOfCurrentUserHolder = Optional.absent();
			}
		}
		return buildQueryPersonalizationOfCurrentUserHolder.orNull();
	}

	@Nullable
	public PackQueryPersonalization getPackQueryPersonalizationOfCurrentUser() {
		if (packQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getAuthUser();
			if (user != null) {
				PackQueryPersonalization personalization =
						OneDev.getInstance(PackQueryPersonalizationService.class).find(this, user);
				if (personalization == null) {
					personalization = new PackQueryPersonalization();
					personalization.setProject(this);
					personalization.setUser(user);
				}
				packQueryPersonalizationOfCurrentUserHolder = Optional.of(personalization);
			} else {
				packQueryPersonalizationOfCurrentUserHolder = Optional.absent();
			}
		}
		return packQueryPersonalizationOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public Iteration getHierarchyIteration(@Nullable String iterationName) {
		for (Iteration iteration: getHierarchyIterations()) {
			if (iteration.getName().equals(iterationName))
				return iteration;
		}
		return null;
	}
	
	@Nullable
	public Iteration getIteration(String iterationName) {
		for (Iteration iteration: getIterations()) {
			if (iteration.getName().equals(iterationName))
				return iteration;
		}
		return null;
	}
	
	public Collection<String> getReachableBranches(ObjectId commitId) {
		if (reachableBranchesCache == null) 
			reachableBranchesCache = new HashMap<>();
		Collection<String> reachableBranches = reachableBranchesCache.get(commitId);
		if (reachableBranches == null) {
			reachableBranches = new HashSet<>();
			
			CommitInfoService commitInfoService = OneDev.getInstance(CommitInfoService.class);
			Collection<ObjectId> descendants = commitInfoService.getDescendants(
					getId(), Sets.newHashSet(commitId));
			descendants.add(commitId);

			for (RefFacade ref : getBranchRefs()) {
				if (descendants.contains(ref.getPeeledObj()))
					reachableBranches.add(Preconditions.checkNotNull(GitUtils.ref2branch(ref.getName())));
			}
			reachableBranchesCache.put(commitId, reachableBranches);
		}
		return reachableBranches;
	}

	public boolean isCommitOnBranches(@Nullable ObjectId commitId, PatternSet branches) {
		Matcher matcher = new PathMatcher();
		if (commitId != null) 
			return getReachableBranches(commitId).stream().anyMatch(it->branches.matches(matcher, it));
		else 
			return branches.matches(matcher, "main");
	}
	
	public boolean isCommitOnBranch(ObjectId commitId, String branch) {
		return getReachableBranches(commitId).stream().anyMatch(it->matchPath(branch, it));
	}
	
	public boolean isReviewRequiredForModification(User user, String branch, @Nullable String file) {
		return getBranchProtection(branch, user).isReviewRequiredForModification(file);
	}
	
	public boolean canModifyBuildSpecRoughly(User user, String branch) {
		return getBranchProtection(branch, user).canModifyBuildSpecRoughly(user);
	}

	public boolean isCommitSignatureRequiredButNoSigningKey(User user, String branch) {
		return getBranchProtection(branch, user).isCommitSignatureRequired()
				&& getSettingService().getGpgSetting().getSigningKey() == null;
	}
	
	public boolean isCommitSignatureRequired(User user, String branch) {
		return getBranchProtection(branch, user).isCommitSignatureRequired();
	}
	
	public boolean isTagSignatureRequired(User user, String tag) {
		return getTagProtection(tag, user).isCommitSignatureRequired();
	}
	
	public boolean isTagSignatureRequiredButNoSigningKey(User user, String tag) {
		return getTagProtection(tag, user).isCommitSignatureRequired()
				&& getSettingService().getGpgSetting().getSigningKey() == null;
	}
	
	public boolean isReviewRequiredForPush(User user, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		return getBranchProtection(branch, user).isReviewRequiredForPush(this, oldObjectId, newObjectId, gitEnvs);
	}
	
	public boolean isBuildRequiredForModification(User user, String branch, @Nullable String file) {
		return getBranchProtection(branch, user).isBuildRequiredForModification(file);
	}
	
	public boolean isBuildRequiredForPush(User user, String branch, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		return getBranchProtection(branch, user).isBuildRequiredForPush(this, oldObjectId, newObjectId, gitEnvs);
	}
	
	@Nullable
	public CommitMessageError checkCommitMessages(String branch, User user, ObjectId oldCommitId, ObjectId newCommitId,
												  Map<String, String> gitEnvs) {
		var protection = getBranchProtection(branch, user);
		return getGitService().checkCommitMessages(protection, this, oldCommitId, newCommitId, gitEnvs);
	}

	public Collection<String> getViolatedFileTypes(String branch, User user, ObjectId oldCommitId, ObjectId newCommitId,
												  Map<String, String> gitEnvs) {
		var protection = getBranchProtection(branch, user);
		return protection.getViolatedFileTypes(this, oldCommitId, newCommitId, gitEnvs);
	}
	
	@Nullable
	public List<String> readLines(BlobIdent blobIdent, WhitespaceOption whitespaceOption, boolean mustExist) {
		Blob blob = getBlob(blobIdent, mustExist);
		if (blob != null) {
			Blob.Text text = blob.getText();
			if (text != null) {
				List<String> normalizedLines = new ArrayList<>();
				for (String line: text.getLines()) 
					normalizedLines.add(whitespaceOption.apply(line));
				return normalizedLines;
			}
		}
		return null;
	}
	
	@Nullable
	public static Project get() {
		if (!stack.get().isEmpty()) {
			return stack.get().peek();
		} else if (Build.get() != null) {
			return Build.get().getProject();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				ProjectAware projectAware = WicketUtils.findInnermost(componentContext.getComponent(), ProjectAware.class);
				if (projectAware != null) 
					return projectAware.getProject();
			}
			return null;
		}
	}
	
	public String getAttachmentUrlPath(String attachmentGroup, String attachmentName) {
		return String.format("/~downloads/projects/%s/attachments/%s/%s", getId(), attachmentGroup, 
				UrlEncoder.PATH_INSTANCE.encode(attachmentName, StandardCharsets.UTF_8));
	}
	
	public boolean isPermittedByLoginUser(Permission permission) {
		return getBaseAuthorizations().stream().anyMatch(it->it.getRole().implies(permission));
	}
	
	public LinkedHashMap<String, ContributedProjectSetting> getContributedSettings() {
		return contributedSettings;
	}

	public void setContributedSettings(LinkedHashMap<String, ContributedProjectSetting> contributedSettings) {
		this.contributedSettings = contributedSettings;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ContributedProjectSetting> T getContributedSetting(Class<T> settingClass) {
		T contributedSetting = (T) contributedSettings.get(settingClass.getName());
		if (contributedSetting == null) {
			try {
				T value = settingClass.getDeclaredConstructor().newInstance();
				if (OneDev.getInstance(Validator.class).validate(value).isEmpty()) 
					contributedSetting = value;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		
		return contributedSetting;
	}

	public void setContributedSetting(Class<? extends ContributedProjectSetting> settingClass, 
			@Nullable ContributedProjectSetting setting) {
		if (setting != null)
			contributedSettings.put(settingClass.getName(), setting);
		else
			contributedSettings.remove(settingClass.getName());
	}
	
	public String calcPath() {
		if (getParent() != null)
			return getParent().calcPath() + "/" + getName();
		else
			return getName();
	}
	
	public boolean isSelfOrAncestorOf(Project project) {
		if (this.equals(project)) 
			return true;
		else if (project.getParent() != null) 
			return isSelfOrAncestorOf(project.getParent());
		else 
			return false;
	}
	
	public Collection<Project> getSelfAndAncestors() {
		Collection<Project> selfAndAncestors = Lists.newArrayList(this);
		Project parent = getParent();
		while (parent != null) {
			selfAndAncestors.add(parent);
			parent = parent.getParent();
		}
		return selfAndAncestors;
	}
	
	@Nullable
	public static String substitutePath(@Nullable String patterns, String oldPath, String newPath) {
		PatternSet patternSet = PatternSet.parse(patterns);
		Set<String> substitutedIncludes = patternSet.getIncludes().stream()
				.map(it->PathUtils.substituteSelfOrAncestor(it, oldPath, newPath))
				.collect(Collectors.toSet());
		Set<String> substitutedExcludes = patternSet.getExcludes().stream()
				.map(it->PathUtils.substituteSelfOrAncestor(it, oldPath, newPath))
				.collect(Collectors.toSet());
		patterns = new PatternSet(substitutedIncludes, substitutedExcludes).toString();
		if (patterns.length() == 0)
			patterns = null;
		return patterns;
	}
	
	public boolean hasValidCommitSignature(RevCommit commit) {
		return getSignatureVerificationService().verifySignature(commit) instanceof VerificationSuccessful;
	}
	
	public boolean hasValidCommitSignature(ObjectId commitId, Map<String, String> gitEnvs) {
		byte[] rawCommit = getGitService().getRawCommit(this, commitId, gitEnvs);
		return getSignatureVerificationService().verifyCommitSignature(rawCommit) 
				instanceof VerificationSuccessful;
	}
	
	public boolean hasValidTagSignature(ObjectId tagId, Map<String, String> gitEnvs) {
		byte[] rawTag = getGitService().getRawTag(this, tagId, gitEnvs);
		return rawTag != null && getSignatureVerificationService().verifyTagSignature(rawTag) instanceof VerificationSuccessful;
	}
	
	public boolean isCommitSignatureRequirementSatisfied(User user, String branch, RevCommit commit) {
		return !isCommitSignatureRequired(user, branch) || hasValidCommitSignature(commit);
	}
	
	private SignatureVerificationService getSignatureVerificationService() {
		return OneDev.getInstance(SignatureVerificationService.class);
	}
	
	public static boolean containsPath(@Nullable String patterns, String path) {
		PatternSet patternSet = PatternSet.parse(patterns);
		return patternSet.getIncludes().stream().anyMatch(it->PathUtils.isSelfOrAncestor(path, it)) 
				|| patternSet.getExcludes().stream().anyMatch(it->PathUtils.isSelfOrAncestor(path, it));
	}
	
	@Nullable
	public NamedIssueQuery getNamedIssueQuery(String name) {
		for (NamedIssueQuery namedQuery: getNamedIssueQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

	@Nullable
	public NamedBuildQuery getNamedBuildQuery(String name) {
		for (NamedBuildQuery namedQuery: getNamedBuildQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

	@Nullable
	public NamedPackQuery getNamedPackQuery(String name) {
		for (NamedPackQuery namedQuery: getNamedPackQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	@Nullable
	public NamedPullRequestQuery getNamedPullRequestQuery(String name) {
		for (NamedPullRequestQuery namedQuery: getNamedPullRequestQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	@Nullable
	public NamedCommitQuery getNamedCommitQuery(String name) {
		for (NamedCommitQuery namedQuery: getNamedCommitQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	public List<NamedIssueQuery> getNamedIssueQueries() {
		Project current = this;
		do {
			List<NamedIssueQuery> namedQueries = current.getIssueSetting().getNamedQueries();
			if (namedQueries != null)
				return namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return getSettingService().getIssueSetting().getNamedQueries();
	}
	
	public List<NamedBuildQuery> getNamedBuildQueries() {
		Project current = this;
		do {
			List<NamedBuildQuery> namedQueries = current.getBuildSetting().getNamedQueries();
			if (namedQueries != null)
				return namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return getSettingService().getBuildSetting().getNamedQueries();
	}

	public List<NamedPackQuery> getNamedPackQueries() {
		Project current = this;
		do {
			List<NamedPackQuery> namedQueries = current.getPackSetting().getNamedQueries();
			if (namedQueries != null)
				return namedQueries;
			current = current.getParent();
		} while (current != null);

		return getSettingService().getPackSetting().getNamedQueries();
	}
	
	public List<NamedPullRequestQuery> getNamedPullRequestQueries() {
		Project current = this;
		do {
			List<NamedPullRequestQuery> namedQueries = current.getPullRequestSetting().getNamedQueries();
			if (namedQueries != null)
				return namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return getSettingService().getPullRequestSetting().getNamedQueries();
	}
	
	public Map<String, TimesheetSetting> getHierarchyTimesheetSettings() {
		Map<String, TimesheetSetting> timesheetSettings = new LinkedHashMap<>();
		Project current = this;
		do {
			for (var entry: current.getIssueSetting().getTimesheetSettings().entrySet()) 
				timesheetSettings.putIfAbsent(entry.getKey(), entry.getValue());
			current = current.getParent();
		} while (current != null);
		return timesheetSettings;
	}
	
	public List<BoardSpec> getHierarchyBoards() {
		List<BoardSpec> boards;
		
		Project current = this;
		do {
			boards = current.getIssueSetting().getBoardSpecs();
			if (boards != null)
				break;
			current = current.getParent();
		} while (current != null);
		
		if (boards == null)
			boards = getSettingService().getIssueSetting().getBoardSpecs();
		return boards;
	}
	
	public Collection<Long> parseFixedIssueIds(String commitMessage) {
		return OneDev.getInstance(IssueService.class).parseFixedIssueIds(this, commitMessage);
	}
	
	public Collection<Project> getTree() {
		List<Project> projects = Lists.newArrayList(this);
		projects.addAll(getDescendants());
		projects.addAll(getAncestors());
		return projects;
	}
	
	public MediaType detectMediaType(BlobIdent blobIdent) {
		Blob blob = getBlob(blobIdent, true);
		if (blob.getLfsPointer() != null) {
			return new LfsObject(getId(), blob.getLfsPointer().getObjectId())
					.detectMediaType(blobIdent.getName());
		} else {
			return blob.getMediaType();
		}
	}
	
	@Nullable
	public String getActiveServer(boolean mustExist) {
		if (activeServer == null) {
			activeServer = Optional.fromNullable(
					getProjectService().getActiveServer(getId(), false));
		}
		if (activeServer.isPresent())
			return activeServer.get();
		else if (mustExist) 
			throw new ExplicitException("Storage not found for project: " + getPath());
		else
			return null;
	}
	
	public String findCodeAnalysisPatterns() {
		Project current = this;
		do {
			if (current.getCodeAnalysisSetting().getAnalyzeFiles() != null)
				return current.getCodeAnalysisSetting().getAnalyzeFiles();
			current = current.getParent();
		} while (current != null);
		
		return "**";
	}

	public MergeStrategy findDefaultPullRequestMergeStrategy() {
		Project current = this;
		do {
			if (current.getPullRequestSetting().getDefaultMergeStrategy() != null)
				return current.getPullRequestSetting().getDefaultMergeStrategy();
			current = current.getParent();
		} while (current != null);

		return MergeStrategy.CREATE_MERGE_COMMIT;
	}

	public boolean findDeleteBranchAfterPullRequestMerge() {
		Project current = this;
		do {
			if (current.getPullRequestSetting().getDeleteSourceBranchAfterMerge() != null)
				return current.getPullRequestSetting().getDeleteSourceBranchAfterMerge();
			current = current.getParent();
		} while (current != null);

		return false;
	}
	
	public List<User> findDefaultPullRequestAssignees() {
		Project current = this;
		do {
			if (!current.getPullRequestSetting().getDefaultAssignees().isEmpty()) {
				var users = new ArrayList<User>();
				for (var userName: current.getPullRequestSetting().getDefaultAssignees()) {
					var user = getUserService().findByName(userName);
					if (user == null)
						throw new ExplicitException("Pull request default assignee not found: " + userName);
					else if (!SecurityUtils.canWriteCode(user.asSubject(), this))
						throw new ExplicitException("Pull request default assignee does not have code write permission over the target project: " + userName);
					users.add(user);
				}
				return users;
			}
			current = current.getParent();
		} while (current != null);

		return new ArrayList<>();
	}
	
	public String getSiteLockName() {
		return getSiteLockName(getId());
	}
	
	public static String getSiteLockName(Long projectId) {
		return "project-site:" + projectId;
	}

	public Collection<Build> getBuilds(ObjectId commitId) {
		if (buildsCache == null)
			buildsCache = new HashMap<>();
		Collection<Build> builds = buildsCache.get(commitId);
		if (builds == null) {
			BuildService buildService = OneDev.getInstance(BuildService.class);
			builds = buildService.query(this, commitId, null, null, null, null, new HashMap<>());
			buildsCache.put(commitId, builds);
		}
		return builds;
	}

	public static String getDeleteChangeObservable(Long projectId) {
		return Project.class.getName() + ":delete:" + projectId;
	}

	public String getDeleteChangeObservable() {
		return getDeleteChangeObservable(getId());
	}
	
	public File getDir() {
		return OneDev.getInstance(ProjectService.class).getProjectDir(getId());
	}
	
	public static String encodePathAsRepoName(String projectPath) {
		return projectPath.replace("/", FAKED_GITHUB_REPO_PATH_SEPARATOR);
	}
	
	public static String decodeRepoNameAsPath(String text) {
		return text.replace(FAKED_GITHUB_REPO_PATH_SEPARATOR, "/"); 
	}

	public static String encodePathAsFullRepoName(String projectPath) {
		return FAKED_GITHUB_REPO_OWNER + "/" + encodePathAsRepoName(projectPath);
	}
	
	public static String decodeFullRepoNameAsPath(String text) {
		return decodeRepoNameAsPath(replace(text, FAKED_GITHUB_REPO_OWNER + "/", ""));
	}
	
	public static Collection<Project> getIndependents(Collection<Project> projects) {
		Collection<Project> independents = new HashSet<>(projects);
		for (Iterator<Project> it = independents.iterator(); it.hasNext(); ) {
			Project independent = it.next();
			for (Project each : independents) {
				if (!each.equals(independent) && each.isSelfOrAncestorOf(independent)) {
					it.remove();
					break;
				}
			}
		}
		return independents;
	}
	
}
