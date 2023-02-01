package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.*;
import io.onedev.server.git.*;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.git.signature.SignatureVerificationKeyLoader;
import io.onedev.server.git.signature.SignatureVerified;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.support.*;
import io.onedev.server.model.support.build.*;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.FileProtection;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.util.validation.annotation.ProjectName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Markdown;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.server.web.util.WicketUtils;
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

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.onedev.server.model.Project.PROP_NAME;

@Entity
@Table(
		indexes={
				@Index(columnList="o_parent_id"), @Index(columnList="o_forkedFrom_id"),
				@Index(columnList="o_dynamics_id"), @Index(columnList=PROP_NAME)
		}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_parent_id", PROP_NAME})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate 
@Editable
public class Project extends AbstractEntity implements LabelSupport<ProjectLabel> {

	private static final long serialVersionUID = 1L;
	
	public static final int MAX_DESCRIPTION_LEN = 15000;
	
	public static final String NAME_NAME = "Name";
	
	public static final String PROP_NAME = "name";
	
	public static final String NAME_PATH = "Path";
	
	public static final String PROP_PATH = "path";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String PROP_FORKED_FROM = "forkedFrom";
	
	public static final String NAME_LAST_ACTIVITY_DATE = "Last Activity Date";

	public static final String NAME_LAST_COMMIT_DATE = "Last Commit Date";
	
	public static final String PROP_DYNAMICS = "dynamics";
	
	public static final String NAME_LABEL = "Label";
	
	public static final String PROP_PARENT = "parent";
	
	public static final String PROP_USER_AUTHORIZATIONS = "userAuthorizations";
	
	public static final String PROP_GROUP_AUTHORIZATIONS = "groupAuthorizations";
	
	public static final String PROP_CODE_MANAGEMENT = "codeManagement";
	
	public static final String PROP_ISSUE_MANAGEMENT = "issueManagement";
	
	public static final String NAME_SERVICE_DESK_NAME = "Service Desk Name";
	
	public static final String PROP_SERVICE_DESK_NAME = "serviceDeskName";
	
	public static final String NULL_SERVICE_DESK_PREFIX = "<$NullServiceDesk$>";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_NAME, NAME_PATH, NAME_LABEL, NAME_SERVICE_DESK_NAME, NAME_DESCRIPTION, NAME_LAST_ACTIVITY_DATE, NAME_LAST_COMMIT_DATE);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_PATH, PROP_PATH,
			NAME_NAME, PROP_NAME, 
			NAME_SERVICE_DESK_NAME, PROP_SERVICE_DESK_NAME,
			NAME_LAST_ACTIVITY_DATE, PROP_DYNAMICS + "." + ProjectDynamics.PROP_LAST_ACTIVITY_DATE,
			NAME_LAST_COMMIT_DATE, PROP_DYNAMICS + "." + ProjectDynamics.PROP_LAST_COMMIT_DATE);
	
	static ThreadLocal<Stack<Project>> stack =  new ThreadLocal<Stack<Project>>() {

		@Override
		protected Stack<Project> initialValue() {
			return new Stack<Project>();
		}
	
	};
	
	public static void push(Project project) {
		stack.get().push(project);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	private static final ReferenceMap<ObjectId, Optional<byte[]>> buildSpecBytesCache = 
			new ReferenceMap<>(ReferenceStrength.HARD, ReferenceStrength.SOFT);

	private transient Map<ObjectId, Optional<BuildSpec>> buildSpecCache;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	@Api(description="Represents the project from which this project is forked. Remove this property if "
			+ "the project is not a fork when create/update the project. May be null")
	private Project forkedFrom;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	@Api(description="Represents the parent project. Remove this property if the project does not " +
			"have a parent project. May be null")
	private Project parent;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(unique=true, nullable=false)
	private ProjectDynamics dynamics;

	@Column(nullable=false)
	private String name;
	
	@JsonIgnore
	@Column(nullable=false)
	private String path;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	@Api(description = "May be null")
	private String description;
	
    @OneToMany(mappedBy="project")
    private Collection<Build> builds = new ArrayList<>();
    
    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
	
    @JsonIgnore
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<TagProtection> tagProtections = new ArrayList<>();

    @JsonIgnore
    @Lob
    @Column(nullable=false, length=65535)
	private LinkedHashMap<String, ContributedProjectSetting> contributedSettings = new LinkedHashMap<>();
	
	@Column(nullable=false)
	@Api(readOnly=true)
	private Date createDate = new Date();
	
	@OneToMany(mappedBy="targetProject", cascade=CascadeType.REMOVE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceProject")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Issue> issues = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<ProjectLabel> labels = new ArrayList<>();
	
    @OneToMany(mappedBy="parent")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> children = new ArrayList<>();
    
    @OneToMany(mappedBy="forkedFrom")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> forks = new ArrayList<>();
    
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	@Api(description="This represents default role of the project. Remove this property if the project should not "
			+ "have a default role when create/update the project. May be null.")
    private Role defaultRole;
    
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> groupAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> userAuthorizations = new ArrayList<>();
	
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
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Milestone> milestones = new ArrayList<>();
	
	private boolean codeManagement = true;
	
	private boolean issueManagement = true;

	@Lob
	@Column(length=65535)
	private GitPackConfig gitPackConfig = new GitPackConfig();
	
	@Lob
	@Column(length=65535)
	private CodeAnalysisSetting codeAnalysisSetting = new CodeAnalysisSetting();
	
	// SQL Server does not allow duplicate null values for unique column. So we use 
	// special prefix to indicate null
	@JsonIgnore
	@Column(unique=true, nullable=false)
	private String serviceDeskName = NULL_SERVICE_DESK_PREFIX + UUID.randomUUID().toString();
	
	@JsonIgnore
	@Lob
	@Column(length=65535, nullable=false)
	private ProjectIssueSetting issueSetting = new ProjectIssueSetting();
	
	@JsonIgnore
	@Lob
	@Column(length=65535, nullable=false)
	private ProjectBuildSetting buildSetting = new ProjectBuildSetting();
	
	@JsonIgnore
	@Lob
	@Column(length=65535, nullable=false)
	private ProjectPullRequestSetting pullRequestSetting = new ProjectPullRequestSetting();
	
	@JsonIgnore
	@Lob
	@Column(length=65535)
	private ArrayList<NamedCommitQuery> namedCommitQueries;
	
	@JsonIgnore
	@Lob
	@Column(length=65535)
	private ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries;
	
	@JsonIgnore
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
    
    private transient Optional<CommitQueryPersonalization> commitQueryPersonalizationOfCurrentUserHolder;
    
	private transient List<Milestone> sortedMilestones;
	
	private transient Optional<UUID> storageServerUUID;

	private transient Map<ObjectId, Collection<Build>> buildsCache;
	
	@Editable(order=100)
	@ProjectName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Editable(order=200)
	@Markdown
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

	public ProjectDynamics getDynamics() {
		return dynamics;
	}

	public void setDynamics(ProjectDynamics dynamics) {
		this.dynamics = dynamics;
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

	@Nullable
	public Role getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(Role defaultRole) {
		this.defaultRole = defaultRole;
	}

	public Collection<GroupAuthorization> getGroupAuthorizations() {
		return groupAuthorizations;
	}

	public void setGroupAuthorizations(Collection<GroupAuthorization> groupAuthorizations) {
		this.groupAuthorizations = groupAuthorizations;
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

	public Collection<Project> getDescendants() {
		Collection<Project> descendants = new ArrayList<>(getChildren());
		for (Project child: getChildren())
			descendants.addAll(child.getDescendants());
		return descendants;
	}

	public Collection<Project> getAncestors() {
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
		List<RefFacade> refs = getRefs(Constants.R_HEADS);
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
		return getRefs(Constants.R_TAGS);
    }
	
	public List<RefFacade> getRefs(String prefix) {
		return getGitService().getRefs(this, prefix);
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
	public List<Project> getForkChildren() {
		List<Project> children = new ArrayList<>();
		for (Project fork: getForks()) {  
			children.add(fork);
			children.addAll(fork.getForkChildren());
		}
		
		return children;
	}
	
	public List<Project> getForkParents() {
		List<Project> forkParents = new ArrayList<>();
		if (getForkedFrom() != null) {
			forkParents.add(getForkedFrom());
			forkParents.addAll(getForkedFrom().getForkParents());
		}
		return forkParents;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}

	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(this);
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
	
	@Override
	public ProjectFacade getFacade() {
		return new ProjectFacade(getId(), getName(), getPath(), getServiceDeskName(), 
				isIssueManagement(), Role.idOf(getDefaultRole()), Project.idOf(getParent()));
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
			throw new ObjectNotFoundException("Unable to find object '" + revision + "'");
		return optional.orNull();
	}
	
	public void cacheObjectId(String revision, @Nullable ObjectId objectId) {
		if (objectIdCache == null)
			objectIdCache = new HashMap<>();
		
		objectIdCache.put(revision, Optional.fromNullable(objectId));
	}

	public Map<String, Status> getCommitStatus(ObjectId commitId, 
			@Nullable String pipeline, @Nullable PullRequest request, @Nullable String refName) {
		Map<String, Collection<StatusInfo>> commitStatusInfos = getCommitStatusCache().get(commitId);
		if (commitStatusInfos == null) {
			BuildManager buildManager = OneDev.getInstance(BuildManager.class);
			commitStatusInfos = buildManager.queryStatus(this, Sets.newHashSet(commitId)).get(commitId);
			getCommitStatusCache().put(commitId, Preconditions.checkNotNull(commitStatusInfos));
		}
		Map<String, Status> commitStatus = new HashMap<>();
		for (Map.Entry<String, Collection<StatusInfo>> entry: commitStatusInfos.entrySet()) {
			Collection<Status> statuses = new ArrayList<>();
			for (StatusInfo statusInfo: entry.getValue()) {
				if ((pipeline == null || pipeline.equals(statusInfo.getPipeline()))
						&& (refName == null || refName.equals(statusInfo.getRefName())) 
						&& Objects.equals(PullRequest.idOf(request), statusInfo.getRequestId())) {
					statuses.add(statusInfo.getStatus());
				}
			}
			commitStatus.put(entry.getKey(), Status.getOverallStatus(statuses));
		}
		return commitStatus;
	}
	
	private Map<ObjectId, Map<String, Collection<StatusInfo>>> getCommitStatusCache() {
		if (commitStatusCache == null)
			commitStatusCache = new HashMap<>();
		return commitStatusCache;
	}
	
	public void cacheCommitStatus(Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatuses) {
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
		Optional<byte[]> buildSpecBytes;
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
			if (buildSpec != null)
				buildSpecBytes = Optional.of(SerializationUtils.serialize(buildSpec));
			else
				buildSpecBytes = Optional.absent();
			synchronized (buildSpecBytesCache) {
				buildSpecBytesCache.put(commitId, buildSpecBytes);
			}
			return buildSpec;
		} else if (buildSpecBytes.isPresent()) {
			return SerializationUtils.deserialize(buildSpecBytes.get());
		} else {
			return null;
		}
	}
	
	@Nullable
	public BuildSpec getBuildSpec(ObjectId commitId) {
		if (buildSpecCache == null)
			buildSpecCache = new HashMap<>();
		Optional<BuildSpec> buildSpec = buildSpecCache.get(commitId);
		if (buildSpec == null) {
			buildSpec = Optional.fromNullable(loadBuildSpec(commitId));
			buildSpecCache.put(commitId, buildSpec);
		}
		return buildSpec.orNull();
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
	public RefFacade getBranchRef(String revision) {
		RefFacade ref = getRef(revision);
		if (ref != null && ref.getName().startsWith(Constants.R_HEADS))
			return ref;
		else
			return null;
	}
	
	@Nullable
	public RefFacade getTagRef(String revision) {
		RefFacade ref = getRef(revision);
		if (ref != null && ref.getName().startsWith(Constants.R_TAGS))
			return ref;
		else
			return null;
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
	
	@Nullable
	public String getServiceDeskName() {
		if (serviceDeskName.startsWith(NULL_SERVICE_DESK_PREFIX))
			return null;
		else
			return serviceDeskName;
	}

	public void setServiceDeskName(@Nullable String serviceDeskName) {
		if (serviceDeskName != null)
			this.serviceDeskName = serviceDeskName;
		else if (!this.serviceDeskName.startsWith(NULL_SERVICE_DESK_PREFIX))
			this.serviceDeskName = NULL_SERVICE_DESK_PREFIX + UUID.randomUUID().toString();
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

	public List<JobSecret> getHierarchyJobSecrets() {
		List<JobSecret> jobSecrets = new ArrayList<>(getBuildSetting().getJobSecrets());
		if (getParent() != null) {
			Set<String> names = jobSecrets.stream().map(it->it.getName()).collect(Collectors.toSet());
			for (JobSecret secret: getParent().getHierarchyJobSecrets()) {
				if (!names.contains(secret.getName()))
					jobSecrets.add(secret);
			}
		}
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
	
	public List<ActionAuthorization> getHierarchyActionAuthorizations() {
		List<ActionAuthorization> actionAuthorizations = new ArrayList<>(getBuildSetting().getActionAuthorizations());
		if (getParent() != null)
			actionAuthorizations.addAll(getParent().getHierarchyActionAuthorizations());
		return actionAuthorizations;
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

	public Collection<Build> getBuilds() {
		return builds;
	}

	public void setBuilds(Collection<Build> builds) {
		this.builds = builds;
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

	public Collection<Milestone> getMilestones() {
		return milestones;
	}
	
	public Collection<Milestone> getHierarchyMilestones() {
		Collection<Milestone> milestones = new ArrayList<>(getMilestones());
		if (getParent() != null)
			milestones.addAll(getParent().getHierarchyMilestones());
		return milestones;
	}

	public void setMilestones(Collection<Milestone> milestones) {
		this.milestones = milestones;
	}

	public List<Milestone> getSortedHierarchyMilestones() {
		if (sortedMilestones == null) {
			sortedMilestones = new ArrayList<>(getHierarchyMilestones());
			Collections.sort(sortedMilestones, new Milestone.DatesAndStatusComparator());
		}
		return sortedMilestones;
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

	public TagProtection getHierarchyTagProtection(String tagName, User user) {
		boolean noCreation = false;
		boolean noDeletion = false;
		boolean noUpdate = false;
		boolean signatureRequired = false;
		for (TagProtection protection: getHierarchyTagProtections()) {
			if (protection.isEnabled() 
					&& UserMatch.parse(protection.getUserMatch()).matches(this, user)
					&& PatternSet.parse(protection.getTags()).matches(new PathMatcher(), tagName)) {
				noCreation = noCreation || protection.isPreventCreation();
				noDeletion = noDeletion || protection.isPreventDeletion();
				noUpdate = noUpdate || protection.isPreventUpdate();
				signatureRequired = signatureRequired || protection.isSignatureRequired();
			}
		}
		
		TagProtection protection = new TagProtection();
		protection.setPreventCreation(noCreation);
		protection.setPreventDeletion(noDeletion);
		protection.setPreventUpdate(noUpdate);
		protection.setSignatureRequired(signatureRequired);
		
		return protection;
	}
	
	public BranchProtection getHierarchyBranchProtection(String branchName, @Nullable User user) {
		boolean noCreation = false;
		boolean noDeletion = false;
		boolean noForcedPush = false;
		boolean signatureRequired = false;
		
		Set<String> jobNames = new HashSet<>();
		List<FileProtection> fileProtections = new ArrayList<>();
		ReviewRequirement reviewRequirement = ReviewRequirement.parse(null, true);
		for (BranchProtection protection: getHierarchyBranchProtections()) {
			if (protection.isEnabled() 
					&& UserMatch.parse(protection.getUserMatch()).matches(this, user) 
					&& PatternSet.parse(protection.getBranches()).matches(new PathMatcher(), branchName)) {
				noCreation = noCreation || protection.isPreventCreation();
				noDeletion = noDeletion || protection.isPreventDeletion();
				noForcedPush = noForcedPush || protection.isPreventForcedPush();
				signatureRequired = signatureRequired || protection.isSignatureRequired();
				jobNames.addAll(protection.getJobNames());
				fileProtections.addAll(protection.getFileProtections());
				reviewRequirement.mergeWith(protection.getParsedReviewRequirement());
			}
		}
		
		BranchProtection protection = new BranchProtection();
		protection.setFileProtections(fileProtections);
		protection.setJobNames(new ArrayList<>(jobNames));
		protection.setPreventCreation(noCreation);
		protection.setPreventDeletion(noDeletion);
		protection.setPreventForcedPush(noForcedPush);
		protection.setSignatureRequired(signatureRequired);
		protection.setParsedReviewRequirement(reviewRequirement);
		
		return protection;
	}

	@Override
	public String toString() {
		return getPath();
	}

	public List<User> getAuthors(String filePath, ObjectId commitId, @Nullable LinearRange range) {
		List<User> authors = new ArrayList<>();
		EmailAddressManager emailAddressManager = OneDev.getInstance(EmailAddressManager.class);
		for (BlameBlock block: getGitService().blame(this, commitId, filePath, range)) {
			EmailAddress emailAddress = emailAddressManager.findByPersonIdent(block.getCommit().getAuthor());
			if (emailAddress != null && emailAddress.isVerified() && !authors.contains(emailAddress.getOwner()))
				authors.add(emailAddress.getOwner());
		}
		
		return authors;
	}
	
	public IssueQueryPersonalization getIssueQueryPersonalizationOfCurrentUser() {
		if (issueQueryPersonalizationOfCurrentUserHolder == null) {
			User user = SecurityUtils.getUser();
			if (user != null) {
				IssueQueryPersonalization personalization = 
						OneDev.getInstance(IssueQueryPersonalizationManager.class).find(this, user);
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
			User user = SecurityUtils.getUser();
			if (user != null) {
				CommitQueryPersonalization personalization = 
						OneDev.getInstance(CommitQueryPersonalizationManager.class).find(this, user);
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
			User user = SecurityUtils.getUser();
			if (user != null) {
				PullRequestQueryPersonalization personalization = 
						OneDev.getInstance(PullRequestQueryPersonalizationManager.class).find(this, user);
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
			User user = SecurityUtils.getUser();
			if (user != null) {
				CodeCommentQueryPersonalization personalization = 
						OneDev.getInstance(CodeCommentQueryPersonalizationManager.class).find(this, user);
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
			User user = SecurityUtils.getUser();
			if (user != null) {
				BuildQueryPersonalization personalization = 
						OneDev.getInstance(BuildQueryPersonalizationManager.class).find(this, user);
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
	public Milestone getHierarchyMilestone(@Nullable String milestoneName) {
		for (Milestone milestone: getHierarchyMilestones()) {
			if (milestone.getName().equals(milestoneName))
				return milestone;
		}
		return null;
	}
	
	@Nullable
	public Milestone getMilestone(String milestoneName) {
		for (Milestone milestone: getMilestones()) {
			if (milestone.getName().equals(milestoneName))
				return milestone;
		}
		return null;
	}
	
	public boolean isCommitOnBranches(@Nullable ObjectId commitId, String branches) {
		Matcher matcher = new PathMatcher();
		if (commitId != null) {
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
			Collection<ObjectId> descendants = commitInfoManager.getDescendants(
					getId(), Sets.newHashSet(commitId));
			descendants.add(commitId);
		
			PatternSet branchPatterns = PatternSet.parse(branches);
			for (RefFacade ref: getBranchRefs()) {
				String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(ref.getName()));
				if (descendants.contains(ref.getPeeledObj()) && branchPatterns.matches(matcher, branchName))
					return true;
			}
			return false;
		} else {
			return PatternSet.parse(branches).matches(matcher, "main");
		}
	}
	
	public boolean isReviewRequiredForModification(User user, String branch, @Nullable String file) {
		return getHierarchyBranchProtection(branch, user).isReviewRequiredForModification(user, this, branch, file);
	}

	public boolean isCommitSignatureRequiredButNoSigningKey(User user, String branch) {
		return getHierarchyBranchProtection(branch, user).isSignatureRequired()
				&& getSettingManager().getGpgSetting().getSigningKey() == null;
	}
	
	public boolean isCommitSignatureRequired(User user, String branch) {
		return getHierarchyBranchProtection(branch, user).isSignatureRequired();
	}
	
	public boolean isTagSignatureRequired(User user, String tag) {
		return getHierarchyTagProtection(tag, user).isSignatureRequired();
	}
	
	public boolean isTagSignatureRequiredButNoSigningKey(User user, String tag) {
		return getHierarchyTagProtection(tag, user).isSignatureRequired()
				&& getSettingManager().getGpgSetting().getSigningKey() == null;
	}
	
	public boolean isReviewRequiredForPush(User user, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		return getHierarchyBranchProtection(branch, user).isReviewRequiredForPush(user, this, branch, oldObjectId, newObjectId, gitEnvs);
	}
	
	public boolean isBuildRequiredForModification(User user, String branch, @Nullable String file) {
		return getHierarchyBranchProtection(branch, user).isBuildRequiredForModification(this, branch, file);
	}
	
	public boolean isBuildRequiredForPush(User user, String branch, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		return getHierarchyBranchProtection(branch, user).isBuildRequiredForPush(this, oldObjectId, newObjectId, gitEnvs);
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
		return getDefaultRole() != null && getDefaultRole().implies(permission);
	}
	
	public LinkedHashMap<String, ContributedProjectSetting> getContributedSettings() {
		return contributedSettings;
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
		contributedSettings.put(settingClass.getName(), setting);
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
		SignatureVerificationKeyLoader keyLoader = 
				OneDev.getInstance(SignatureVerificationKeyLoader.class);
		return GitUtils.verifySignature(commit, keyLoader) instanceof SignatureVerified;
	}
	
	public boolean hasValidCommitSignature(ObjectId commitId, Map<String, String> gitEnvs) {
		byte[] commitRawData = getGitService().getRawCommit(this, commitId, gitEnvs);
		SignatureVerificationKeyLoader keyLoader = OneDev.getInstance(SignatureVerificationKeyLoader.class);
		return GitUtils.verifyCommitSignature(commitRawData, keyLoader) instanceof SignatureVerified;
	}
	
	public boolean hasValidTagSignature(ObjectId tagId, Map<String, String> gitEnvs) {
		byte[] tagRawData = getGitService().getRawTag(this, tagId, gitEnvs);
		SignatureVerificationKeyLoader keyLoader = OneDev.getInstance(SignatureVerificationKeyLoader.class);
		return tagRawData != null && GitUtils.verifyTagSignature(tagRawData, keyLoader) instanceof SignatureVerified;
	}
	
	public boolean isCommitSignatureRequirementSatisfied(User user, String branch, RevCommit commit) {
		return !isCommitSignatureRequired(user, branch) || hasValidCommitSignature(commit);
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
	public NamedPullRequestQuery getNamedPullRequestQuery(String name) {
		for (NamedPullRequestQuery namedQuery: getNamedPullRequestQueries()) {
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
				return (ArrayList<NamedIssueQuery>) namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return (ArrayList<NamedIssueQuery>) getSettingManager().getIssueSetting().getNamedQueries();
	}
	
	public List<NamedBuildQuery> getNamedBuildQueries() {
		Project current = this;
		do {
			List<NamedBuildQuery> namedQueries = current.getBuildSetting().getNamedQueries();
			if (namedQueries != null)
				return (ArrayList<NamedBuildQuery>) namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return (ArrayList<NamedBuildQuery>) getSettingManager().getBuildSetting().getNamedQueries();
	}
	
	public List<NamedPullRequestQuery> getNamedPullRequestQueries() {
		Project current = this;
		do {
			List<NamedPullRequestQuery> namedQueries = current.getPullRequestSetting().getNamedQueries();
			if (namedQueries != null)
				return (ArrayList<NamedPullRequestQuery>) namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return (ArrayList<NamedPullRequestQuery>) getSettingManager().getPullRequestSetting().getNamedQueries();
	}

	public List<BoardSpec> getHierarchyBoards() {
		List<BoardSpec> boards = null;
		
		Project current = this;
		do {
			boards = current.getIssueSetting().getBoardSpecs();
			if (boards != null)
				break;
			current = current.getParent();
		} while (current != null);
		
		if (boards == null)
			boards = getSettingManager().getIssueSetting().getBoardSpecs();
		return boards;
	}
	
	public Collection<Long> parseFixedIssueIds(String commitMessage) {
		return OneDev.getInstance(IssueManager.class).parseFixedIssueIds(this, commitMessage);
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
	public UUID getStorageServerUUID(boolean mustExist) {
		if (storageServerUUID == null) {
			storageServerUUID = Optional.fromNullable(
					getProjectManager().getStorageServerUUID(getId(), false));
		}
		if (storageServerUUID.isPresent())
			return storageServerUUID.get();
		else if (mustExist) 
			throw new ExplicitException("Storage not found for project: " + getPath());
		else
			return null;
	}
	
	public PatternSet findCodeAnalysisPatterns() {
		Project current = this;
		do {
			if (current.getCodeAnalysisSetting().getAnalyzeFiles() != null)
				return PatternSet.parse(current.getCodeAnalysisSetting().getAnalyzeFiles());
			current = current.getParent();
		} while (current != null);

		return PatternSet.parse("**");
	}

	public MergeStrategy findDefaultMergeStrategy() {
		Project current = this;
		do {
			if (current.getPullRequestSetting().getDefaultMergeStrategy() != null)
				return current.getPullRequestSetting().getDefaultMergeStrategy();
			current = current.getParent();
		} while (current != null);

		return MergeStrategy.CREATE_MERGE_COMMIT;
	}

	public boolean findPullRequestWithLFS() {
		Project current = this;
		do {
			if (current.getPullRequestSetting().isWithLFS() != null)
				return current.getPullRequestSetting().isWithLFS();
			current = current.getParent();
		} while (current != null);

		return false;
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
			BuildManager buildManager = OneDev.getInstance(BuildManager.class);
			builds = buildManager.query(this, commitId, null, null, null, new HashMap<>(), null);
			buildsCache.put(commitId, builds);
		}
		return builds;
	}

}
