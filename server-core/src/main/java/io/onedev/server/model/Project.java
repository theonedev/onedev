package io.onedev.server.model;

import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_UPDATE_DATE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
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

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
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

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildQuerySettingManager;
import io.onedev.server.entitymanager.CodeCommentQuerySettingManager;
import io.onedev.server.entitymanager.CommitQuerySettingManager;
import io.onedev.server.entitymanager.IssueQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestQuerySettingManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.git.Submodule;
import io.onedev.server.git.command.BlameCommand;
import io.onedev.server.git.command.ListChangedFilesCommand;
import io.onedev.server.git.exception.NotFileException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.FileProtection;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.util.validation.annotation.ProjectName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Markdown;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.server.web.util.WicketUtils;

@Entity
@Table(indexes={@Index(columnList="o_forkedFrom_id"), @Index(columnList=PROP_NAME), 
		@Index(columnList=PROP_UPDATE_DATE)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate 
@Editable
public class Project extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME_NAME = "Name";
	
	public static final String PROP_NAME = "name";
	
	public static final String NAME_UPDATE_DATE = "Update Date";
	
	public static final String PROP_UPDATE_DATE = "updateDate";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String PROP_ID = "id";
	
	public static final String PROP_FORKED_FROM = "forkedFrom";
	
	public static final String PROP_USER_AUTHORIZATIONS = "userAuthorizations";
	
	public static final String PROP_GROUP_AUTHORIZATIONS = "groupAuthorizations";
	
	public static final List<String> QUERY_FIELDS = 
			Lists.newArrayList(NAME_NAME, NAME_DESCRIPTION, NAME_UPDATE_DATE);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_NAME, PROP_NAME, 
			NAME_UPDATE_DATE, PROP_UPDATE_DATE);
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	public static final int MAX_UPLOAD_SIZE = 10; // In mega bytes
	
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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Project forkedFrom;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private User owner;
	
	@Column(nullable=false, unique=true)
	private String name;
	
	@Column(length=15000)
	private String description;
	
    @OneToMany(mappedBy="project")
    private Collection<Build> builds = new ArrayList<>();
    
	@Lob
	@Column(nullable=false, length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<TagProtection> tagProtections = new ArrayList<>();
	
	@Column(nullable=false)
	private Date createDate = new Date();
	
	@Column(nullable=false)
	private Date updateDate = new Date();

	@OneToMany(mappedBy="targetProject", cascade=CascadeType.REMOVE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceProject")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Issue> issues = new ArrayList<>();
	
    @OneToMany(mappedBy="forkedFrom")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Project> forks = new ArrayList<>();
    
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> groupAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<UserAuthorization> userAuthorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeComment> codeComments = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<IssueQuerySetting> userIssueQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CommitQuerySetting> userCommitQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<PullRequestQuerySetting> userPullRequestQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentQuerySetting> userCodeCommentQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<BuildQuerySetting> userBuildQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Milestone> milestones = new ArrayList<>();
	
	private boolean issueManagementEnabled = true;
	
	@Lob
	@Column(length=65535, nullable=false)
	@JsonView(DefaultView.class)
	private ProjectIssueSetting issueSetting = new ProjectIssueSetting();
	
	@Lob
	@Column(length=65535, nullable=false)
	@JsonView(DefaultView.class)
	private ProjectBuildSetting buildSetting = new ProjectBuildSetting();
	
	@Lob
	@Column(length=65535, nullable=false)
	@JsonView(DefaultView.class)
	private ProjectPullRequestSetting pullRequestSetting = new ProjectPullRequestSetting();
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedCommitQuery> namedCommitQueries;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries;
	
	@Lob
	@Column(length=65535, nullable=false)
	@JsonView(DefaultView.class)
	private ArrayList<WebHook> webHooks = new ArrayList<>();
	
	private transient Repository repository;
	
    private transient Map<BlobIdent, Optional<Blob>> blobCache;
    
    private transient Map<String, Optional<ObjectId>> objectIdCache;
    
    private transient Map<ObjectId, Optional<BuildSpec>> buildSpecCache;
    
    private transient Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatusCache;
    
    private transient Map<ObjectId, Optional<RevCommit>> commitCache;
    
    private transient Map<String, Optional<Ref>> refCache;
    
    private transient Optional<String> defaultBranchOptional;
    
    private transient Optional<IssueQuerySetting> issueQuerySettingOfCurrentUserHolder;
    
    private transient Optional<PullRequestQuerySetting> pullRequestQuerySettingOfCurrentUserHolder;
    
    private transient Optional<CodeCommentQuerySetting> codeCommentQuerySettingOfCurrentUserHolder;
    
    private transient Optional<BuildQuerySetting> buildQuerySettingOfCurrentUserHolder;
    
    private transient Optional<CommitQuerySetting> commitQuerySettingOfCurrentUserHolder;
    
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
		this.description = description;
	}

	public ArrayList<BranchProtection> getBranchProtections() {
		return branchProtections;
	}

	public void setBranchProtections(ArrayList<BranchProtection> branchProtections) {
		this.branchProtections = branchProtections;
	}

	public ArrayList<TagProtection> getTagProtections() {
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
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl() + "/projects/" + getName();
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
									if (mustExist)
										throw new ObjectNotFoundException("Unable to find submodule '" + blobIdent.path + "' in .gitmodules");
									else
										blob = Optional.absent();
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
			@Nullable PullRequest request, @Nullable String refName) {
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
				if ((refName == null || refName.equals(statusInfo.getRefName())) 
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
	 * 			build spec of specified commit, or <tt>null</tt> if no build spec is defined and 
	 * 			auto-detection also can not provide an appropriate build spec  
	 * @throws
	 * 			Exception when build spec is defined but not valid
	 */
	@Nullable
	public BuildSpec getBuildSpec(ObjectId commitId) {
		if (buildSpecCache == null)
			buildSpecCache = new HashMap<>();
		Optional<BuildSpec> buildSpec = buildSpecCache.get(commitId);
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
		
		Blob blob = getBlob(new BlobIdent(revision, ".gitmodules", FileMode.REGULAR_FILE.getBits()), true);
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
	    	OneDev.getInstance(TransactionManager.class).runAfterCommit(new Runnable() {

				@Override
				public void run() {
			    	OneDev.getInstance(SessionManager.class).runAsync(new Runnable() {

						@Override
						public void run() {
							Project project = OneDev.getInstance(ProjectManager.class).load(getId());
							OneDev.getInstance(ListenerRegistry.class).post(
									new RefUpdated(project, refName, ObjectId.zeroId(), commitId));
						}
			    		
			    	});
				}
	    		
	    	});			
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void createTag(String tagName, String tagRevision, PersonIdent taggerIdent, @Nullable String tagMessage) {
		try {
			TagCommand tag = git().tag();
			tag.setName(tagName);
			if (tagMessage != null)
				tag.setMessage(tagMessage);
			tag.setTagger(taggerIdent);
			tag.setObjectId(getRevCommit(tagRevision, true));
			tag.call();
			
			String refName = GitUtils.tag2ref(tagName);
			cacheObjectId(refName, tag.getObjectId());
			
	    	ObjectId commitId = tag.getObjectId().copy();
	    	OneDev.getInstance(TransactionManager.class).runAfterCommit(new Runnable() {

				@Override
				public void run() {
			    	OneDev.getInstance(SessionManager.class).runAsync(new Runnable() {

						@Override
						public void run() {
							Project project = OneDev.getInstance(ProjectManager.class).load(getId());
							OneDev.getInstance(ListenerRegistry.class).post(
									new RefUpdated(project, refName, ObjectId.zeroId(), commitId));
						}
			    		
			    	});
				}
	    		
	    	});			
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    }
    
	public Collection<CodeComment> getCodeComments() {
		return codeComments;
	}

	public void setCodeComments(Collection<CodeComment> codeComments) {
		this.codeComments = codeComments;
	}
	
	@Editable(order=300, name="Issue management", description="Whether or not to provide issue management for the project")
	public boolean isIssueManagementEnabled() {
		return issueManagementEnabled;
	}
	
	public void setIssueManagementEnabled(boolean issueManagementEnabled) {
		this.issueManagementEnabled = issueManagementEnabled;
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
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("All", null));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Created by me", "created by me"));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Created recently", "\"Create Date\" is since \"last week\""));
			namedCodeCommentQueries.add(new NamedCodeCommentQuery("Updated recently", "\"Update Date\" is since \"last week\""));
		}
		return namedCodeCommentQueries;
	}

	public void setNamedCodeCommentQueries(ArrayList<NamedCodeCommentQuery> namedCodeCommentQueries) {
		this.namedCodeCommentQueries = namedCodeCommentQueries;
	}
	
	public Collection<IssueQuerySetting> getUserIssueQuerySettings() {
		return userIssueQuerySettings;
	}

	public void setUserIssueQuerySettings(Collection<IssueQuerySetting> userIssueQuerySettings) {
		this.userIssueQuerySettings = userIssueQuerySettings;
	}

	public Collection<CommitQuerySetting> getUserCommitQuerySettings() {
		return userCommitQuerySettings;
	}

	public void setUserCommitQuerySettings(Collection<CommitQuerySetting> userCommitQuerySettings) {
		this.userCommitQuerySettings = userCommitQuerySettings;
	}

	public Collection<PullRequestQuerySetting> getUserPullRequestQuerySettings() {
		return userPullRequestQuerySettings;
	}

	public void setUserPullRequestQuerySettings(Collection<PullRequestQuerySetting> userPullRequestQuerySettings) {
		this.userPullRequestQuerySettings = userPullRequestQuerySettings;
	}

	public Collection<CodeCommentQuerySetting> getUserCodeCommentQuerySettings() {
		return userCodeCommentQuerySettings;
	}

	public void setUserCodeCommentQuerySettings(Collection<CodeCommentQuerySetting> userCodeCommentQuerySettings) {
		this.userCodeCommentQuerySettings = userCodeCommentQuerySettings;
	}
	
	public Collection<BuildQuerySetting> getUserBuildQuerySettings() {
		return userBuildQuerySettings;
	}

	public void setUserBuildQuerySettings(Collection<BuildQuerySetting> userBuildQuerySettings) {
		this.userBuildQuerySettings = userBuildQuerySettings;
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

	public void setMilestones(Collection<Milestone> milestones) {
		this.milestones = milestones;
	}

	public List<Milestone> getSortedMilestones() {
		if (sortedMilestones == null) {
			sortedMilestones = new ArrayList<>();
			List<Milestone> open = getMilestones().stream()
					.filter(it->!it.isClosed())
					.sorted(new Milestone.DueDateComparator())
					.collect(Collectors.toList());
			sortedMilestones.addAll(open);
			List<Milestone> closed = getMilestones().stream()
					.filter(it->it.isClosed())
					.sorted(new Milestone.DueDateComparator())
					.collect(Collectors.toList());
			Collections.reverse(closed);
			sortedMilestones.addAll(closed);
		}
		return sortedMilestones;
	}
	
	@Editable
	public ArrayList<WebHook> getWebHooks() {
		return webHooks;
	}

	public void setWebHooks(ArrayList<WebHook> webHooks) {
		this.webHooks = webHooks;
	}

	public TagProtection getTagProtection(String tagName, User user) {
		boolean noCreation = false;
		boolean noDeletion = false;
		boolean noUpdate = false;
		for (TagProtection protection: getTagProtections()) {
			if (protection.isEnabled() 
					&& UserMatch.parse(protection.getUserMatch()).matches(this, user)
					&& PatternSet.parse(protection.getTags()).matches(new PathMatcher(), tagName)) {
				noCreation = noCreation || protection.isPreventCreation();
				noDeletion = noDeletion || protection.isPreventDeletion();
				noUpdate = noUpdate || protection.isPreventUpdate();
			}
		}
		
		TagProtection protection = new TagProtection();
		protection.setPreventCreation(noCreation);
		protection.setPreventDeletion(noDeletion);
		protection.setPreventUpdate(noUpdate);
		
		return protection;
	}
	
	public BranchProtection getBranchProtection(String branchName, @Nullable User user) {
		boolean noCreation = false;
		boolean noDeletion = false;
		boolean noForcedPush = false;
		Set<String> jobNames = new HashSet<>();
		List<FileProtection> fileProtections = new ArrayList<>();
		ReviewRequirement reviewRequirement = ReviewRequirement.parse(null, true);
		for (BranchProtection protection: getBranchProtections()) {
			if (protection.isEnabled() 
					&& UserMatch.parse(protection.getUserMatch()).matches(this, user) 
					&& PatternSet.parse(protection.getBranches()).matches(new PathMatcher(), branchName)) {
				noCreation = noCreation || protection.isPreventCreation();
				noDeletion = noDeletion || protection.isPreventDeletion();
				noForcedPush = noForcedPush || protection.isPreventForcedPush();
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
		protection.setParsedReviewRequirement(reviewRequirement);
		
		return protection;
	}

	@Override
	public String toString() {
		return getName();
	}

	public List<User> getAuthors(String filePath, ObjectId commitId, @Nullable LinearRange range) {
		BlameCommand cmd = new BlameCommand(getGitDir());
		cmd.commitHash(commitId.name());
		cmd.file(filePath);
		cmd.range(range);

		List<User> authors = new ArrayList<>();
		UserManager userManager = OneDev.getInstance(UserManager.class);
		for (BlameBlock block: cmd.call()) {
			User author = userManager.find(block.getCommit().getAuthor());
			if (author != null && !authors.contains(author))
				authors.add(author);
		}
		
		return authors;
	}
	
	public IssueQuerySetting getIssueQuerySettingOfCurrentUser() {
		if (issueQuerySettingOfCurrentUserHolder == null) {
			User user = SecurityUtils.getUser();
			if (user != null) {
				IssueQuerySetting setting = OneDev.getInstance(IssueQuerySettingManager.class).find(this, user);
				if (setting == null) {
					setting = new IssueQuerySetting();
					setting.setProject(this);
					setting.setUser(user);
				}
				issueQuerySettingOfCurrentUserHolder = Optional.of(setting);
			} else {
				issueQuerySettingOfCurrentUserHolder = Optional.absent();
			}
		}
		return issueQuerySettingOfCurrentUserHolder.orNull();
	}
	
	public CommitQuerySetting getCommitQuerySettingOfCurrentUser() {
		if (commitQuerySettingOfCurrentUserHolder == null) {
			User user = SecurityUtils.getUser();
			if (user != null) {
				CommitQuerySetting setting = OneDev.getInstance(CommitQuerySettingManager.class).find(this, user);
				if (setting == null) {
					setting = new CommitQuerySetting();
					setting.setProject(this);
					setting.setUser(user);
				}
				commitQuerySettingOfCurrentUserHolder = Optional.of(setting);
			} else {
				commitQuerySettingOfCurrentUserHolder = Optional.absent();
			}
		}
		return commitQuerySettingOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public PullRequestQuerySetting getPullRequestQuerySettingOfCurrentUser() {
		if (pullRequestQuerySettingOfCurrentUserHolder == null) {
			User user = SecurityUtils.getUser();
			if (user != null) {
				PullRequestQuerySetting setting = OneDev.getInstance(PullRequestQuerySettingManager.class).find(this, user);
				if (setting == null) {
					setting = new PullRequestQuerySetting();
					setting.setProject(this);
					setting.setUser(user);
				}
				pullRequestQuerySettingOfCurrentUserHolder = Optional.of(setting);
			} else {
				pullRequestQuerySettingOfCurrentUserHolder = Optional.absent();
			}
		}
		return pullRequestQuerySettingOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public CodeCommentQuerySetting getCodeCommentQuerySettingOfCurrentUser() {
		if (codeCommentQuerySettingOfCurrentUserHolder == null) {
			User user = SecurityUtils.getUser();
			if (user != null) {
				CodeCommentQuerySetting setting = OneDev.getInstance(CodeCommentQuerySettingManager.class).find(this, user);
				if (setting == null) {
					setting = new CodeCommentQuerySetting();
					setting.setProject(this);
					setting.setUser(user);
				}
				codeCommentQuerySettingOfCurrentUserHolder = Optional.of(setting);
			} else {
				codeCommentQuerySettingOfCurrentUserHolder = Optional.absent();
			}
		}
		return codeCommentQuerySettingOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public BuildQuerySetting getBuildQuerySettingOfCurrentUser() {
		if (buildQuerySettingOfCurrentUserHolder == null) {
			User user = SecurityUtils.getUser();
			if (user != null) {
				BuildQuerySetting setting = OneDev.getInstance(BuildQuerySettingManager.class).find(this, user);
				if (setting == null) {
					setting = new BuildQuerySetting();
					setting.setProject(this);
					setting.setUser(user);
				}
				buildQuerySettingOfCurrentUserHolder = Optional.of(setting);
			} else {
				buildQuerySettingOfCurrentUserHolder = Optional.absent();
			}
		}
		return buildQuerySettingOfCurrentUserHolder.orNull();
	}
	
	@Nullable
	public Milestone getMilestone(@Nullable String milestoneName) {
		for (Milestone milestone: milestones) {
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
		return getBranchProtection(branch, user).isReviewRequiredForModification(user, this, branch, file);
	}

	public boolean isReviewRequiredForPush(User user, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		return getBranchProtection(branch, user).isReviewRequiredForPush(user, this, branch, oldObjectId, newObjectId, gitEnvs);
	}
	
	public boolean isBuildRequiredForModification(User user, String branch, @Nullable String file) {
		return getBranchProtection(branch, user).isBuildRequiredForModification(this, branch, file);
	}
	
	public boolean isBuildRequiredForPush(User user, String branch, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		return getBranchProtection(branch, user).isBuildRequiredForPush(this, oldObjectId, newObjectId, gitEnvs);
	}
	
	@Nullable
	public List<String> readLines(BlobIdent blobIdent, WhitespaceOption whitespaceOption, boolean mustExist) {
		Blob blob = getBlob(blobIdent, mustExist);
		if (blob != null) {
			Blob.Text text = blob.getText();
			if (text != null) {
				List<String> normalizedLines = new ArrayList<>();
				for (String line: text.getLines()) 
					normalizedLines.add(whitespaceOption.process(line));
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
	
}
