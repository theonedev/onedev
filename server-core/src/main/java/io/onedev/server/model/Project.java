package io.onedev.server.model;

import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_UPDATE_DATE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Validator;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.Permission;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildQueryPersonalizationManager;
import io.onedev.server.entitymanager.CodeCommentQueryPersonalizationManager;
import io.onedev.server.entitymanager.CommitQueryPersonalizationManager;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestQueryPersonalizationManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.git.Submodule;
import io.onedev.server.git.command.BlameCommand;
import io.onedev.server.git.command.GetRawCommitCommand;
import io.onedev.server.git.command.GetRawTagCommand;
import io.onedev.server.git.command.ListChangedFilesCommand;
import io.onedev.server.git.exception.NotFileException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.git.signature.SignatureVerificationKeyLoader;
import io.onedev.server.git.signature.SignatureVerified;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.FileProtection;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.model.support.build.DefaultFixedIssueFilter;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageManager;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.AttachmentTooLargeException;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ContentDetector;
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

@Entity
@Table(
		indexes={
				@Index(columnList="o_parent_id"), @Index(columnList="o_forkedFrom_id"), 
				@Index(columnList=PROP_NAME), @Index(columnList=PROP_UPDATE_DATE)
		}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_parent_id", PROP_NAME})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate 
@Editable
public class Project extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Project.class);
	
	public static final int MAX_DESCRIPTION_LEN = 15000;
	
	private static final int BUFFER_SIZE = 1024*64;
	
	public static final String NAME_NAME = "Name";
	
	public static final String PROP_NAME = "name";
	
	public static final String NAME_PATH = "Path";
	
	public static final String NAME_UPDATE_DATE = "Update Date";
	
	public static final String PROP_UPDATE_DATE = "updateDate";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String PROP_FORKED_FROM = "forkedFrom";
	
	public static final String PROP_PARENT = "parent";
	
	public static final String PROP_USER_AUTHORIZATIONS = "userAuthorizations";
	
	public static final String PROP_GROUP_AUTHORIZATIONS = "groupAuthorizations";
	
	public static final String PROP_CODE_MANAGEMENT = "codeManagement";
	
	public static final String PROP_ISSUE_MANAGEMENT = "issueManagement";
	
	public static final String NAME_SERVICE_DESK_NAME = "Service Desk Name";
	
	public static final String PROP_SERVICE_DESK_NAME = "serviceDeskName";
	
	public static final String NULL_SERVICE_DESK_PREFIX = "<$NullServiceDesk$>";
	
	public static final List<String> QUERY_FIELDS = 
			Lists.newArrayList(NAME_NAME, NAME_PATH, NAME_SERVICE_DESK_NAME, NAME_DESCRIPTION, NAME_UPDATE_DATE);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_NAME, PROP_NAME, 
			NAME_SERVICE_DESK_NAME, PROP_SERVICE_DESK_NAME,
			NAME_UPDATE_DATE, PROP_UPDATE_DATE);
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
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
	
	private static final ReferenceMap<ObjectId, Optional<BuildSpec>> buildSpecCache = 
			new ReferenceMap<>(ReferenceStrength.HARD, ReferenceStrength.SOFT);
    
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	@Api(description="Represents the project from which this project is forked. Remove this property if "
			+ "the project is not a fork when create/update the project")
	private Project forkedFrom;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	@Api(description="Represents the parent project. Remove this property if the project does not have a parent project")
	private Project parent;
	
	@Column(nullable=false)
	private String name;
	
	@Column(length=MAX_DESCRIPTION_LEN)
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
	
	@Column(nullable=false)
	@Api(readOnly=true)
	private Date updateDate = new Date();

	@OneToMany(mappedBy="targetProject", cascade=CascadeType.REMOVE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceProject")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Issue> issues = new ArrayList<>();
	
    @OneToMany(mappedBy="parent")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> children = new ArrayList<>();
    
    @OneToMany(mappedBy="forkedFrom")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> forks = new ArrayList<>();
    
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	@Api(description="This represents default role of the project. Remove this property if the project should not "
			+ "have a default role when create/update the project")
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
	
	private transient Repository repository;
	
    private transient Map<BlobIdent, Optional<Blob>> blobCache;
    
    private transient Map<String, Optional<ObjectId>> objectIdCache;
    
    private transient Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatusCache;
    
    private transient Map<ObjectId, Optional<RevCommit>> commitCache;
    
    private transient Map<String, Optional<Ref>> refCache;
    
    private transient Optional<String> defaultBranchOptional;
    
    private transient Optional<IssueQueryPersonalization> issueQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<PullRequestQueryPersonalization> pullRequestQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<CodeCommentQueryPersonalization> codeCommentQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<BuildQueryPersonalization> buildQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<CommitQueryPersonalization> commitQueryPersonalizationOfCurrentUserHolder;
    
    private transient Optional<RevCommit> lastCommitHolder;
    
	private transient List<Milestone> sortedMilestones;
	
	@Editable(order=100)
	@ProjectName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
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
	
	public List<RefInfo> getBranchRefInfos() {
		List<RefInfo> refInfos = getRefInfos(Constants.R_HEADS);
		for (Iterator<RefInfo> it = refInfos.iterator(); it.hasNext();) {
			RefInfo refInfo = it.next();
			if (refInfo.getRef().getName().equals(GitUtils.branch2ref(getDefaultBranch()))) {
				it.remove();
				refInfos.add(0, refInfo);
				break;
			}
		}
		
		return refInfos;
    }
	
	public List<RefInfo> getTagRefInfos() {
		return getRefInfos(Constants.R_TAGS);
    }
	
	public List<RefInfo> getRefInfos(String prefix) {
		try (RevWalk revWalk = new RevWalk(getRepository())) {
			List<Ref> refs = new ArrayList<Ref>(getRepository().getRefDatabase().getRefsByPrefix(prefix));
			List<RefInfo> refInfos = refs.stream()
					.map(ref->new RefInfo(revWalk, ref))
					.filter(refInfo->refInfo.getPeeledObj() instanceof RevCommit)
					.collect(Collectors.toList());
			Collections.sort(refInfos);
			Collections.reverse(refInfos);
			return refInfos;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

	public Git git() {
		return Git.wrap(getRepository()); 
	}
	
	public File getGitDir() {
		return OneDev.getInstance(StorageManager.class).getProjectGitDir(getId());
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
	
	public Repository getRepository() {
		if (repository == null) 
			repository = OneDev.getInstance(ProjectManager.class).getRepository(this);
		return repository;
	}
	
	public String getUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl() + "/projects/" + getId();
	}
	
	@Nullable
	public String getDefaultBranch() {
		if (defaultBranchOptional == null) {
			try {
				Ref headRef = getRepository().findRef("HEAD");
				if (headRef != null 
						&& headRef.isSymbolic() 
						&& headRef.getTarget().getName().startsWith(Constants.R_HEADS) 
						&& headRef.getObjectId() != null) {
					defaultBranchOptional = Optional.of(Repository.shortenRefName(headRef.getTarget().getName()));
				} else {
					defaultBranchOptional = Optional.absent();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return defaultBranchOptional.orNull();
	}
	
	public void setDefaultBranch(String defaultBranchName) {
		RefUpdate refUpdate = GitUtils.getRefUpdate(getRepository(), "HEAD");
		GitUtils.linkRef(refUpdate, GitUtils.branch2ref(defaultBranchName));
		defaultBranchOptional = null;
		
		OneDev.getInstance(JobManager.class).schedule(this);
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
		return new ProjectFacade(getId(), getName(), getServiceDeskName(), isIssueManagement(), 
				Role.idOf(getDefaultRole()), Project.idOf(getParent()));
	}
	
	/**
	 * Read blob content and cache result in repository in case the same blob 
	 * content is requested again. 
	 * 
	 * We made this method thread-safe as we are using ForkJoinPool to calculate 
	 * diffs of multiple blob changes concurrently, and this method will be 
	 * accessed concurrently in that special case.
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
		
		Optional<Blob> blob = getBlobCache().get(blobIdent);
		if (blob == null) {
			try (RevWalk revWalk = new RevWalk(getRepository())) {
				ObjectId revId = getObjectId(blobIdent.revision, mustExist);		
				if (revId != null) {
					RevCommit commit = GitUtils.parseCommit(revWalk, revId);
					if (commit != null) {
						RevTree revTree = commit.getTree();
						TreeWalk treeWalk = TreeWalk.forPath(getRepository(), blobIdent.path, revTree);
						if (treeWalk != null) {
							ObjectId blobId = treeWalk.getObjectId(0);
							if (blobIdent.isGitLink()) {
								String url = getSubmodules(blobIdent.revision).get(blobIdent.path);
								if (url == null) {
									logger.error("Unable to find submodule (project: {}, revision: {}, path: {})", 
											getPath(), blobIdent.revision, blobIdent.path);
									blob = Optional.of(new Blob(blobIdent, blobId, treeWalk.getObjectReader()));
								} else {
									String hash = blobId.name();
									blob = Optional.of(new Blob(blobIdent, blobId, new Submodule(url, hash).toString().getBytes()));
								}
							} else if (blobIdent.isTree()) {
								throw new NotFileException("Path '" + blobIdent.path + "' is a tree");
							} else {
								blob = Optional.of(new Blob(blobIdent, blobId, treeWalk.getObjectReader()));
							}
						} 
					} 				
				} 
				if (blob == null) {
					if (mustExist)
						throw new ObjectNotFoundException("Unable to find blob ident: " + blobIdent);
					else 
						blob = Optional.absent();
				}
				getBlobCache().put(blobIdent, blob);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return blob.orNull();
	}
	
	public InputStream getInputStream(BlobIdent ident) {
		try (RevWalk revWalk = new RevWalk(getRepository())) {
			ObjectId commitId = getObjectId(ident.revision, true);
			RevTree revTree = revWalk.parseCommit(commitId).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(getRepository(), ident.path, revTree);
			if (treeWalk != null) {
				ObjectLoader objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
				return objectLoader.openStream();
			} else {
				throw new ObjectNotFoundException("Unable to find blob path '" + ident.path + "' in revision '" + ident.revision + "'");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
			optional = Optional.fromNullable(GitUtils.resolve(getRepository(), revision));
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
	public BuildSpec getBuildSpec(ObjectId commitId) {
		Optional<BuildSpec> buildSpec;
		synchronized (buildSpecCache) {
			buildSpec = buildSpecCache.get(commitId);
		}
		if (buildSpec == null) {
			Blob blob = getBlob(new BlobIdent(commitId.name(), BuildSpec.BLOB_PATH, FileMode.TYPE_FILE), false);
			if (blob != null) {  
				buildSpec = Optional.fromNullable(BuildSpec.parse(blob.getBytes()));
			} else { 
				Blob oldBlob = getBlob(new BlobIdent(commitId.name(), ".onedev-buildspec", FileMode.TYPE_FILE), false);
				if (oldBlob != null)
					buildSpec = Optional.fromNullable(BuildSpec.parse(oldBlob.getBytes()));
				else
					buildSpec = Optional.absent();
			}
			synchronized (buildSpecCache) {
				buildSpecCache.put(commitId, buildSpec);
			}
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
	
	public RevCommit getLastCommit() {
		if (lastCommitHolder == null) {
			RevCommit lastCommit = null;
			try {
				for (Ref ref: getRepository().getRefDatabase().getRefsByPrefix(Constants.R_HEADS)) {
					RevCommit commit = getRevCommit(ref.getObjectId(), false);
					if (commit != null) {
						if (lastCommit != null) {
							if (commit.getCommitTime() > lastCommit.getCommitTime())
								lastCommit = commit;
						} else {
							lastCommit = commit;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			lastCommitHolder = Optional.fromNullable(lastCommit);
		}
		return lastCommitHolder.orNull();
	}
	
	public LastCommitsOfChildren getLastCommitsOfChildren(String revision, @Nullable String path) {
		if (path == null)
			path = "";
		
		final File cacheDir = new File(
				OneDev.getInstance(StorageManager.class).getProjectInfoDir(getId()), 
				"last_commits/" + path + "/onedev_last_commits");
		
		final ReadWriteLock lock;
		try {
			lock = LockUtils.getReadWriteLock(cacheDir.getCanonicalPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		final Set<ObjectId> commitIds = new HashSet<>(); 
		
		lock.readLock().lock();
		try {
			if (cacheDir.exists()) {
				for (String each: cacheDir.list()) 
					commitIds.add(ObjectId.fromString(each));
			} 	
		} finally {
			lock.readLock().unlock();
		}
		
		org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache cache;
		if (!commitIds.isEmpty()) {
			cache = new org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache() {
	
				@SuppressWarnings("unchecked")
				@Override
				public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
					if (commitIds.contains(commitId)) {
						lock.readLock().lock();
						try {
							byte[] bytes = FileUtils.readFileToByteArray(new File(cacheDir, commitId.name()));
							return (Map<String, Value>) SerializationUtils.deserialize(bytes);
						} catch (IOException e) {
							throw new RuntimeException(e);
						} finally {
							lock.readLock().unlock();
						}
					} else {
						return null;
					}
				}
				
			};
		} else {
			cache = null;
		}

		final AnyObjectId commitId = getObjectId(revision, true);
		
		long time = System.currentTimeMillis();
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(getRepository(), commitId, path, cache);
		long elapsed = System.currentTimeMillis()-time;
		if (elapsed > LAST_COMMITS_CACHE_THRESHOLD) {
			lock.writeLock().lock();
			try {
				if (!cacheDir.exists())
					FileUtils.createDir(cacheDir);
				FileUtils.writeByteArrayToFile(
						new File(cacheDir, commitId.name()), 
						SerializationUtils.serialize(lastCommits));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				lock.writeLock().unlock();
			}
		}
		return lastCommits;
	}

	@Nullable
	public Ref getRef(String revision) {
		if (refCache == null)
			refCache = new HashMap<>();
		Optional<Ref> optional = refCache.get(revision);
		if (optional == null) {
			try {
				optional = Optional.fromNullable(getRepository().findRef(revision));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			refCache.put(revision, optional);
		}
		return optional.orNull();
	}
	
	@Nullable
	public String getRefName(String revision) {
		Ref ref = getRef(revision);
		return ref != null? ref.getName(): null;
	}
	
	@Nullable
	public Ref getBranchRef(String revision) {
		Ref ref = getRef(revision);
		if (ref != null && ref.getName().startsWith(Constants.R_HEADS))
			return ref;
		else
			return null;
	}
	
	@Nullable
	public Ref getTagRef(String revision) {
		Ref ref = getRef(revision);
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
		RevCommit commit;
		Optional<RevCommit> optional = commitCache.get(revId);
		if (optional == null) {
			try (RevWalk revWalk = new RevWalk(getRepository())) {
				optional = Optional.fromNullable(GitUtils.parseCommit(revWalk, revId));
			}
			commitCache.put(revId, optional);
		}
		commit = optional.orNull();
		
		if (mustExist && commit == null)
			throw new ObjectNotFoundException("Unable to find commit associated with object id: " + revId);
		else
			return commit;
	}
	
	public List<Ref> getRefs(String prefix) {
		try {
			return getRepository().getRefDatabase().getRefsByPrefix(prefix);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public Map<String, String> getSubmodules(String revision) {
		Map<String, String> submodules = new HashMap<>();
		
		Blob blob = getBlob(new BlobIdent(revision, ".gitmodules", FileMode.REGULAR_FILE.getBits()), false);
		if (blob != null) {
			String content = new String(blob.getBytes());
			
			String path = null;
			String url = null;
			
			for (String line: StringUtils.splitAndTrim(content, "\r\n")) {
				if (line.startsWith("[") && line.endsWith("]")) {
					if (path != null && url != null)
						submodules.put(path, url);
					
					path = url = null;
				} else if (line.startsWith("path")) {
					path = StringUtils.substringAfter(line, "=").trim();
				} else if (line.startsWith("url")) {
					url = StringUtils.substringAfter(line, "=").trim();
				}
			}
			if (path != null && url != null)
				submodules.put(path, url);
		}
		
		return submodules;
	}
    
    public void createBranch(String branchName, String branchRevision) {
		try {
			CreateBranchCommand command = git().branchCreate();
			command.setName(branchName);
			RevCommit commit = getRevCommit(branchRevision, true);
			command.setStartPoint(getRevCommit(branchRevision, true));
			command.call();
			String refName = GitUtils.branch2ref(branchName); 
			cacheObjectId(refName, commit);
			
	    	ObjectId commitId = commit.copy();
	    	OneDev.getInstance(SessionManager.class).runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					Project project = OneDev.getInstance(ProjectManager.class).load(getId());
					OneDev.getInstance(ListenerRegistry.class).post(
							new RefUpdated(project, refName, ObjectId.zeroId(), commitId));
				}
	    		
	    	});			
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void createTag(String tagName, String tagRevision, PersonIdent taggerIdent, 
    		@Nullable String tagMessage, @Nullable PGPSecretKeyRing signingKey) {
		try (	RevWalk revWalk = new RevWalk(getRepository()); 
				ObjectInserter inserter = getRepository().newObjectInserter();) {
			TagBuilder tagBuilder = new TagBuilder();
			tagBuilder.setTag(tagName);
			if (tagMessage != null) {
				if (!tagMessage.endsWith("\n"))
					tagMessage += "\n";
				tagBuilder.setMessage(tagMessage);
			}
			tagBuilder.setTagger(taggerIdent);
			
			RevCommit commit = getRevCommit(tagRevision, true);
			tagBuilder.setObjectId(commit);

			if (signingKey != null) 
				GitUtils.sign(tagBuilder, signingKey);

			ObjectId tagId = inserter.insert(tagBuilder);
			inserter.flush();

			String refName = GitUtils.tag2ref(tagName);
			RefUpdate refUpdate = getRepository().updateRef(refName);
			refUpdate.setNewObjectId(tagId);
			GitUtils.updateRef(refUpdate);

			cacheObjectId(refName, commit);
			
	    	ObjectId commitId = commit.copy();
	    	OneDev.getInstance(SessionManager.class).runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					Project project = OneDev.getInstance(ProjectManager.class).load(getId());
					OneDev.getInstance(ListenerRegistry.class).post(
							new RefUpdated(project, refName, ObjectId.zeroId(), commitId));
				}
	    		
	    	});			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Created recently", "\"Create Date\" is since \"last week\""));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Updated recently", "\"Update Date\" is since \"last week\""));
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

	public List<BlobIdent> getChildren(BlobIdent blobIdent, BlobIdentFilter blobIdentFilter) {
		return getChildren(blobIdent, blobIdentFilter, getObjectId(blobIdent.revision, true));
	}
	
	public List<BlobIdent> getChildren(BlobIdent blobIdent, BlobIdentFilter blobIdentFilter, ObjectId commitId) {
		Repository repository = getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevTree revTree = revWalk.parseCommit(commitId).getTree();
			
			TreeWalk treeWalk;
			if (blobIdent.path != null) {
				treeWalk = TreeWalk.forPath(repository, blobIdent.path, revTree);
				treeWalk.enterSubtree();
			} else {
				treeWalk = new TreeWalk(repository);
				treeWalk.addTree(revTree);
			}
			
			List<BlobIdent> children = new ArrayList<>();
			while (treeWalk.next()) { 
				BlobIdent child = new BlobIdent(blobIdent.revision, treeWalk.getPathString(), treeWalk.getRawMode(0)); 
				if (blobIdentFilter.filter(child))
					children.add(child);
			}
			Collections.sort(children);
			return children;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int getMode(String revision, @Nullable String path) {
		if (path != null) {
			RevCommit commit = getRevCommit(revision, true);
			try {
				TreeWalk treeWalk = TreeWalk.forPath(getRepository(), path, commit.getTree());
				if (treeWalk != null) {
					return treeWalk.getRawMode(0);
				} else {
					throw new ObjectNotFoundException("Unable to find blob path '" + path
							+ "' in revision '" + revision + "'");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return FileMode.TREE.getBits();
		}
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
		BlameCommand cmd = new BlameCommand(getGitDir());
		cmd.commitHash(commitId.name());
		cmd.file(filePath);
		cmd.range(range);

		List<User> authors = new ArrayList<>();
		EmailAddressManager emailAddressManager = OneDev.getInstance(EmailAddressManager.class);
		for (BlameBlock block: cmd.call()) {
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
	
	public boolean isCommitOnBranches(@Nullable ObjectId commitId, String branches) {
		Matcher matcher = new PathMatcher();
		if (commitId != null) {
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
			Collection<ObjectId> descendants = commitInfoManager.getDescendants(this, Sets.newHashSet(commitId));
			descendants.add(commitId);
		
			PatternSet branchPatterns = PatternSet.parse(branches);
			for (RefInfo ref: getBranchRefInfos()) {
				String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(ref.getRef().getName()));
				if (descendants.contains(ref.getPeeledObj()) && branchPatterns.matches(matcher, branchName))
					return true;
			}
			return false;
		} else {
			return PatternSet.parse(branches).matches(matcher, "master");
		}
	}
	
	public Collection<String> getChangedFiles(ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		if (gitEnvs != null && !gitEnvs.isEmpty()) {
			ListChangedFilesCommand cmd = new ListChangedFilesCommand(getGitDir(), gitEnvs);
			cmd.fromRev(oldObjectId.name()).toRev(newObjectId.name());
			return cmd.call();
		} else {
			return GitUtils.getChangedFiles(getRepository(), oldObjectId, newObjectId);
		}
	}
	
	public boolean isReviewRequiredForModification(User user, String branch, @Nullable String file) {
		return getHierarchyBranchProtection(branch, user).isReviewRequiredForModification(user, this, branch, file);
	}

	public boolean isCommitSignatureRequiredButNoSigningKey(User user, String branch) {
		return getHierarchyBranchProtection(branch, user).isSignatureRequired()
				&& OneDev.getInstance(SettingManager.class).getGpgSetting().getSigningKey() == null;
	}
	
	public boolean isCommitSignatureRequired(User user, String branch) {
		return getHierarchyBranchProtection(branch, user).isSignatureRequired();
	}
	
	public boolean isTagSignatureRequired(User user, String tag) {
		return getHierarchyTagProtection(tag, user).isSignatureRequired();
	}
	
	public boolean isTagSignatureRequiredButNoSigningKey(User user, String tag) {
		return getHierarchyTagProtection(tag, user).isSignatureRequired()
				&& OneDev.getInstance(SettingManager.class).getGpgSetting().getSigningKey() == null;
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
		return String.format("/projects/%d/attachment/%s/%s", getId(), attachmentGroup, 
				UrlEncoder.PATH_INSTANCE.encode(attachmentName, StandardCharsets.UTF_8));
	}
	
	public String saveAttachment(String attachmentGroup, String suggestedAttachmentName, InputStream attachmentStream) {
		String attachmentName = suggestedAttachmentName;
		File attachmentDir = OneDev.getInstance(AttachmentStorageManager.class).getGroupDir(this, attachmentGroup);

		FileUtils.createDir(attachmentDir);
		int index = 2;
		while (new File(attachmentDir, attachmentName).exists()) {
			if (suggestedAttachmentName.contains(".")) {
				String nameBeforeExt = StringUtils.substringBeforeLast(suggestedAttachmentName, ".");
				String ext = StringUtils.substringAfterLast(suggestedAttachmentName, ".");
				attachmentName = nameBeforeExt + "_" + index + "." + ext;
			} else {
				attachmentName = suggestedAttachmentName + "_" + index;
			}
			index++;
		}
		
		long maxUploadFileSize = OneDev.getInstance(SettingManager.class)
				.getPerformanceSetting().getMaxUploadFileSize()*1L*1024*1024; 
				
		Exception ex = null;
		File file = new File(attachmentDir, attachmentName);
		try (OutputStream os = new FileOutputStream(file)) {
			byte[] buffer = new byte[BUFFER_SIZE];
	        long count = 0;
	        int n = 0;
	        while (-1 != (n = attachmentStream.read(buffer))) {
	            count += n;
		        if (count > maxUploadFileSize) {
		        	throw new AttachmentTooLargeException("Upload must be less than " 
		        			+ FileUtils.byteCountToDisplaySize(maxUploadFileSize));
		        }
	            os.write(buffer, 0, n);
	        }
		} catch (Exception e) {
			ex = e;
		} 
		if (ex != null) {
			if (file.exists())
				FileUtils.deleteFile(file);
			throw ExceptionUtils.unchecked(ex);
		} else {
			return file.getName();
		}
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
				T value = settingClass.newInstance();
				if (OneDev.getInstance(Validator.class).validate(value).isEmpty()) 
					contributedSetting = value;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		return contributedSetting;
	}

	public void setContributedSetting(Class<? extends ContributedProjectSetting> settingClass, 
			@Nullable ContributedProjectSetting setting) {
		contributedSettings.put(settingClass.getName(), setting);
	}
	
	public File getLfsObjectsDir() {
		return new File(getGitDir(), "lfs/objects");
	}
	
	public File getLfsObjectFile(String objectId) {
		File objectDir = new File(getLfsObjectsDir(), objectId.substring(0, 2) + "/" + objectId.substring(2, 4));
		Lock lock = LockUtils.getLock("lfs-storage:" + getGitDir().getAbsolutePath());
		lock.lock();
		try {
			FileUtils.createDir(objectDir);
		} finally {
			lock.unlock();
		}
		return new File(objectDir, objectId);
	}
	
	public ReadWriteLock getLfsObjectLock(String objectId) {
		return LockUtils.getReadWriteLock("lfs-objects:" + objectId);
	}

	public boolean isLfsObjectExists(String objectId) {
		Lock readLock = getLfsObjectLock(objectId).readLock();
		readLock.lock();
		try {
			return getLfsObjectFile(objectId).exists();
		} finally {
			readLock.unlock();
		}
	}
	
	public InputStream getLfsObjectInputStream(String objectId) {
		Lock readLock = getLfsObjectLock(objectId).readLock();
		readLock.lock();
		try {
			return new FilterInputStream(new FileInputStream(getLfsObjectFile(objectId))) {

				@Override
				public void close() throws IOException {
					super.close();
					readLock.unlock();
				}
				
			};
		} catch (FileNotFoundException e) {
			readLock.unlock();
			throw new RuntimeException(e);
		}
	}
	
	public MediaType detectLfsObjectMediaType(String objectId, String fileName) {
		try (InputStream is = getLfsObjectInputStream(objectId)) {
			return ContentDetector.detectMediaType(is, fileName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MediaType detectMediaType(BlobIdent blobIdent) {
		Blob blob = getBlob(blobIdent, true);
		if (blob.getLfsPointer() != null)
			return detectLfsObjectMediaType(blob.getLfsPointer().getObjectId(), blobIdent.getName());
		else
			return blob.getMediaType();
	}

	public String getPath() {
		if (getParent() != null)
			return getParent().getPath() + "/" + getName();
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
		SignatureVerificationKeyLoader keyLoader = OneDev.getInstance(SignatureVerificationKeyLoader.class);
		return GitUtils.verifySignature(commit, keyLoader) instanceof SignatureVerified;
	}
	
	public boolean hasValidCommitSignature(ObjectId commitId, Map<String, String> gitEnvs) {
		GetRawCommitCommand cmd = new GetRawCommitCommand(getGitDir(), gitEnvs);
		cmd.revision(commitId.name());
		byte[] commitRawData = cmd.call();
		SignatureVerificationKeyLoader keyLoader = OneDev.getInstance(SignatureVerificationKeyLoader.class);
		return GitUtils.verifyCommitSignature(commitRawData, keyLoader) instanceof SignatureVerified;
	}
	
	public boolean hasValidTagSignature(ObjectId tagId, Map<String, String> gitEnvs) {
		GetRawTagCommand cmd = new GetRawTagCommand(getGitDir(), gitEnvs);
		cmd.revision(tagId.name());
		byte[] tagRawData = cmd.call();
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
		
		return (ArrayList<NamedIssueQuery>) OneDev.getInstance(SettingManager.class).getIssueSetting().getNamedQueries();
	}
	
	public List<NamedBuildQuery> getNamedBuildQueries() {
		Project current = this;
		do {
			List<NamedBuildQuery> namedQueries = current.getBuildSetting().getNamedQueries();
			if (namedQueries != null)
				return (ArrayList<NamedBuildQuery>) namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return (ArrayList<NamedBuildQuery>) OneDev.getInstance(SettingManager.class).getBuildSetting().getNamedQueries();
	}
	
	public List<NamedPullRequestQuery> getNamedPullRequestQueries() {
		Project current = this;
		do {
			List<NamedPullRequestQuery> namedQueries = current.getPullRequestSetting().getNamedQueries();
			if (namedQueries != null)
				return (ArrayList<NamedPullRequestQuery>) namedQueries;
			current = current.getParent();
		} while (current != null); 
		
		return (ArrayList<NamedPullRequestQuery>) OneDev.getInstance(SettingManager.class)
				.getPullRequestSetting().getNamedQueries();
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
			boards = OneDev.getInstance(SettingManager.class).getIssueSetting().getBoardSpecs();
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
	
}
