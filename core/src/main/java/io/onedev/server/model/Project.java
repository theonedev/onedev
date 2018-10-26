package io.onedev.server.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import javax.persistence.Version;
import javax.validation.Valid;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
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

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.git.Submodule;
import io.onedev.server.git.command.BlameCommand;
import io.onedev.server.git.exception.NotFileException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.manager.BuildQuerySettingManager;
import io.onedev.server.manager.CodeCommentQuerySettingManager;
import io.onedev.server.manager.CommitQuerySettingManager;
import io.onedev.server.manager.IssueQuerySettingManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestQuerySettingManager;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.CommitMessageTransformSetting;
import io.onedev.server.model.support.NamedBuildQuery;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.DefaultPrivilege;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.util.validation.annotation.ProjectName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Markdown;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.utils.ExceptionUtils;
import io.onedev.utils.FileUtils;
import io.onedev.utils.LockUtils;
import io.onedev.utils.PathUtils;
import io.onedev.utils.Range;
import io.onedev.utils.StringUtils;

@Entity
@Table(indexes={@Index(columnList="o_forkedFrom_id")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
@Editable
public class Project extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	public static final int MAX_UPLOAD_SIZE = 10; // In mega bytes
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Project forkedFrom;

	@Column(nullable=false, unique=true)
	private String name;
	
	@Lob
	@Column(length=65535)
	private String description;
	
	private DefaultPrivilege defaultPrivilege;
	
	@Lob
	@Column(length=65535, name="COMMIT_MSG_TRANSFORM")
	@JsonView(DefaultView.class)
	private CommitMessageTransformSetting commitMessageTransformSetting;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * branch and tag protection settings upon project renaming/deletion
	 */
	@Version
	private long version;
	
    @OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
    private Collection<Configuration> configurations = new ArrayList<>();
    
	@Lob
	@Column(nullable=false, length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<TagProtection> tagProtections = new ArrayList<>();
	
	@Column(nullable=false)
	private Date createdAt = new Date();

	@OneToMany(mappedBy="targetProject", cascade=CascadeType.REMOVE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceProject")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Issue> issues = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Team> teams = new ArrayList<>();
	
    @OneToMany(mappedBy="forkedFrom")
	private Collection<Project> forks = new ArrayList<>();
    
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeComment> codeComments = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<IssueQuerySetting> issueQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CommitQuerySetting> commitQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<PullRequestQuerySetting> pullRequestQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentQuerySetting> codeCommentQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<BuildQuerySetting> buildQuerySettings = new ArrayList<>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.REMOVE)
	private Collection<Milestone> milestones = new ArrayList<>();
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private IssueWorkflow issueWorkflow;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedIssueQuery> savedIssueQueries;

	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedCommitQuery> savedCommitQueries;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedPullRequestQuery> savedPullRequestQueries;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedCodeCommentQuery> savedCodeCommentQueries;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<NamedBuildQuery> savedBuildQueries;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<String> issueListFields;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<IssueBoard> issueBoards;
	
	@Lob
	@Column(length=65535)
	@JsonView(DefaultView.class)
	private ArrayList<WebHook> webHooks;
	
	private transient Repository repository;
	
    private transient Map<BlobIdent, Blob> blobCache;
    
    private transient Map<String, Optional<ObjectId>> objectIdCache;
    
    private transient Map<ObjectId, Optional<RevCommit>> commitCache;
    
    private transient Map<String, Optional<Ref>> refCache;
    
    private transient Optional<String> defaultBranchOptional;
    
    private transient Optional<RevCommit> lastCommitOptional;
    
    private transient Optional<IssueQuerySetting> issueQuerySettingOfCurrentUserHolder;
    
    private transient Optional<PullRequestQuerySetting> pullRequestQuerySettingOfCurrentUserHolder;
    
    private transient Optional<CodeCommentQuerySetting> codeCommentQuerySettingOfCurrentUserHolder;
    
    private transient Optional<BuildQuerySetting> buildQuerySettingOfCurrentUserHolder;
    
    private transient Optional<CommitQuerySetting> commitQuerySettingOfCurrentUserHolder;
    
	@Editable(order=100)
	@ProjectName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Optionally describe the project")
	@Markdown
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=300, description="Optionally specify default privilege for users not "
			+ "joining any teams of the project")
	@NameOfEmptyValue("No default privilege")
	public DefaultPrivilege getDefaultPrivilege() {
		return defaultPrivilege;
	}

	public void setDefaultPrivilege(DefaultPrivilege defaultPrivilege) {
		this.defaultPrivilege = defaultPrivilege;
	}

	@Nullable
	@Valid
	public CommitMessageTransformSetting getCommitMessageTransformSetting() {
		return commitMessageTransformSetting;
	}

	public void setCommitMessageTransformSetting(CommitMessageTransformSetting commitMessageTransformSetting) {
		this.commitMessageTransformSetting = commitMessageTransformSetting;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
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

	public Collection<Team> getTeams() {
		return teams;
	}

	public void setTeams(Collection<Team> teams) {
		this.teams = teams;
	}

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
	
	public List<RefInfo> getBranches() {
		return getRefInfos(Constants.R_HEADS);
    }
	
	public List<RefInfo> getTags() {
		return getRefInfos(Constants.R_TAGS);
    }
	
    public Collection<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(Collection<Configuration> configurations) {
		this.configurations = configurations;
	}

	public List<RefInfo> getRefInfos(String prefix) {
		try (RevWalk revWalk = new RevWalk(getRepository())) {
			List<Ref> refs = new ArrayList<Ref>(getRepository().getRefDatabase().getRefs(prefix).values());
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
	public List<Project> getForkDescendants() {
		List<Project> descendants = new ArrayList<>();
		if (getDefaultBranch() != null)
			descendants.add(this);
		for (Project fork: getForks()) { 
			descendants.addAll(fork.getForkDescendants());
		}
		
		return descendants;
	}
	
	public Repository getRepository() {
		if (repository == null) {
			repository = OneDev.getInstance(ProjectManager.class).getRepository(this);
		}
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
	}
	
	private Map<BlobIdent, Blob> getBlobCache() {
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
	public Blob getBlob(BlobIdent blobIdent) {
		Preconditions.checkArgument(blobIdent.revision!=null && blobIdent.path!=null && blobIdent.mode!=null, 
				"Revision, path and mode of ident param should be specified");
		
		Blob blob = getBlobCache().get(blobIdent);
		if (blob == null) {
			try (RevWalk revWalk = new RevWalk(getRepository())) {
				ObjectId revId = getObjectId(blobIdent.revision);		
				RevTree revTree = revWalk.parseCommit(revId).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(getRepository(), blobIdent.path, revTree);
				if (treeWalk != null) {
					ObjectId blobId = treeWalk.getObjectId(0);
					if (blobIdent.isGitLink()) {
						String url = getSubmodules(blobIdent.revision).get(blobIdent.path);
						if (url == null)
							throw new ObjectNotFoundException("Unable to find submodule '" + blobIdent.path + "' in .gitmodules");
						String hash = blobId.name();
						blob = new Blob(blobIdent, blobId, new Submodule(url, hash).toString().getBytes());
					} else if (blobIdent.isTree()) {
						throw new NotFileException("Path '" + blobIdent.path + "' is a tree");
					} else {
						blob = new Blob(blobIdent, blobId, treeWalk.getObjectReader());
					}
					getBlobCache().put(blobIdent, blob);
				} else {
					throw new ObjectNotFoundException("Unable to find blob path '" + blobIdent.path + "' in revision '" + blobIdent.revision + "'");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return blob;
	}
	
	public InputStream getInputStream(BlobIdent ident) {
		try (RevWalk revWalk = new RevWalk(getRepository())) {
			ObjectId commitId = getObjectId(ident.revision);
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
	
	public ObjectId getObjectId(String revision) {
		return getObjectId(revision, true);
	}
	
	public void cacheObjectId(String revision, @Nullable ObjectId objectId) {
		if (objectIdCache == null)
			objectIdCache = new HashMap<>();
		
		objectIdCache.put(revision, Optional.fromNullable(objectId));
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

		final AnyObjectId commitId = getObjectId(revision);
		
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
	
	public RevCommit getRevCommit(String revision) {
		return getRevCommit(revision, true);
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
	
	public RevCommit getRevCommit(ObjectId revId) {
		return getRevCommit(revId, true);
	}
	
	public Map<String, Ref> getRefs(String prefix) {
		try {
			return getRepository().getRefDatabase().getRefs(prefix);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public Map<String, String> getSubmodules(String revision) {
		Map<String, String> submodules = new HashMap<>();
		
		Blob blob = getBlob(new BlobIdent(revision, ".gitmodules", FileMode.REGULAR_FILE.getBits()));
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

    public void deleteBranch(String branch) {
    	String refName = GitUtils.branch2ref(branch);
    	ObjectId commitId = getObjectId(refName);
    	try {
			git().branchDelete().setForce(true).setBranchNames(branch).call();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
    	
    	Subject subject = SecurityUtils.getSubject();
    	OneDev.getInstance(UnitOfWork.class).doAsync(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					Project project = OneDev.getInstance(ProjectManager.class).load(getId());
					OneDev.getInstance(ListenerRegistry.class).post(
							new RefUpdated(project, refName, commitId, ObjectId.zeroId()));
				} finally {
					ThreadContext.unbindSubject();
				}
			}
    		
    	});
    }
    
    public void createBranch(String branchName, String branchRevision) {
		try {
			CreateBranchCommand command = git().branchCreate();
			command.setName(branchName);
			RevCommit commit = getRevCommit(branchRevision);
			command.setStartPoint(getRevCommit(branchRevision));
			command.call();
			cacheObjectId(GitUtils.branch2ref(branchName), commit);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void tag(String tagName, String tagRevision, PersonIdent taggerIdent, @Nullable String tagMessage) {
		try {
			TagCommand tag = git().tag();
			tag.setName(tagName);
			if (tagMessage != null)
				tag.setMessage(tagMessage);
			tag.setTagger(taggerIdent);
			tag.setObjectId(getRevCommit(tagRevision));
			tag.call();
			cacheObjectId(GitUtils.tag2ref(tagName), tag.getObjectId());
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void deleteTag(String tag) {
    	String refName = GitUtils.tag2ref(tag);
    	ObjectId commitId = getRevCommit(refName).getId();
    	try {
			git().tagDelete().setTags(tag).call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    	Subject subject = SecurityUtils.getSubject();
    	OneDev.getInstance(UnitOfWork.class).doAsync(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					Project project = OneDev.getInstance(ProjectManager.class).load(getId());
					OneDev.getInstance(ListenerRegistry.class).post(
							new RefUpdated(project, refName, commitId, ObjectId.zeroId()));
				} finally {
					ThreadContext.unbindSubject();
				}
			}
    		
    	});
    }
    
	public Collection<CodeComment> getCodeComments() {
		return codeComments;
	}

	public void setCodeComments(Collection<CodeComment> codeComments) {
		this.codeComments = codeComments;
	}

	public IssueWorkflow getIssueWorkflow() {
		if (issueWorkflow == null) 
			issueWorkflow = new IssueWorkflow();
		return issueWorkflow;
	}

	public void setIssueWorkflow(IssueWorkflow issueWorkflow) {
		this.issueWorkflow = issueWorkflow;
	}

	public ArrayList<NamedIssueQuery> getSavedIssueQueries() {
		if (savedIssueQueries == null) {
			savedIssueQueries = new ArrayList<>();
			savedIssueQueries.add(new NamedIssueQuery("Outstanding", "outstanding"));
			savedIssueQueries.add(new NamedIssueQuery("My outstanding", "outstanding and mine"));
			savedIssueQueries.add(new NamedIssueQuery("Submitted recently", "\"Submit Date\" is after \"last week\""));
			savedIssueQueries.add(new NamedIssueQuery("Updated recently", "\"Update Date\" is after \"last week\""));
			savedIssueQueries.add(new NamedIssueQuery("Submitted by me", "submitted by me"));
			savedIssueQueries.add(new NamedIssueQuery("Assigned to me", "\"Assignee\" is me"));
			savedIssueQueries.add(new NamedIssueQuery("Critical outstanding", "outstanding and \"Priority\" is \"Critical\""));
			savedIssueQueries.add(new NamedIssueQuery("Unassigned outstanding", "outstanding and \"Assignee\" is empty"));
			savedIssueQueries.add(new NamedIssueQuery("Closed", "closed"));
			savedIssueQueries.add(new NamedIssueQuery("All", "all"));
		}
		return savedIssueQueries;
	}

	public void setSavedIssueQueries(ArrayList<NamedIssueQuery> savedIssueQueries) {
		this.savedIssueQueries = savedIssueQueries;
	}
	
	public ArrayList<NamedCommitQuery> getSavedCommitQueries() {
		if (savedCommitQueries == null) {
			savedCommitQueries = new ArrayList<>();
			savedCommitQueries.add(new NamedCommitQuery("All", "all"));
			savedCommitQueries.add(new NamedCommitQuery("Default branch", "default-branch"));
			savedCommitQueries.add(new NamedCommitQuery("Authored by me", "authored-by-me"));
			savedCommitQueries.add(new NamedCommitQuery("Committed by me", "committed-by-me"));
			savedCommitQueries.add(new NamedCommitQuery("Committed recently", "after(last week)"));
		}
		return savedCommitQueries;
	}

	public void setSavedCommitQueries(ArrayList<NamedCommitQuery> savedCommitQueries) {
		this.savedCommitQueries = savedCommitQueries;
	}
	
	public ArrayList<NamedPullRequestQuery> getSavedPullRequestQueries() {
		if (savedPullRequestQueries == null) {
			savedPullRequestQueries = new ArrayList<>();
			savedPullRequestQueries.add(new NamedPullRequestQuery("Open", "open"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("To be reviewed by me", "to be reviewed by me"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("To be changed by me", "submitted by me and someone requested for changes"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("Request for changes by me", "requested for changes by me"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("Approved by me", "approved by me"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("Submitted by me", "submitted by me"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("Submitted recently", "\"Submit Date\" is after \"last week\""));
			savedPullRequestQueries.add(new NamedPullRequestQuery("Updated recently", "\"Update Date\" is after \"last week\""));
			savedPullRequestQueries.add(new NamedPullRequestQuery("Closed", "merged or discarded"));
			savedPullRequestQueries.add(new NamedPullRequestQuery("All", "all"));
		}
		return savedPullRequestQueries;
	}

	public void setSavedPullRequestQueries(ArrayList<NamedPullRequestQuery> savedPullRequestQueries) {
		this.savedPullRequestQueries = savedPullRequestQueries;
	}
	
	@Nullable
	public NamedIssueQuery getSavedIssueQuery(String name) {
		for (NamedIssueQuery namedQuery: getSavedIssueQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

	@Nullable
	public NamedCommitQuery getSavedCommitQuery(String name) {
		for (NamedCommitQuery namedQuery: getSavedCommitQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	@Nullable
	public NamedPullRequestQuery getSavedPullRequestQuery(String name) {
		for (NamedPullRequestQuery namedQuery: getSavedPullRequestQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	@Nullable
	public NamedCodeCommentQuery getSavedCodeCommentQuery(String name) {
		for (NamedCodeCommentQuery namedQuery: getSavedCodeCommentQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	@Nullable
	public NamedBuildQuery getSavedBuildQuery(String name) {
		for (NamedBuildQuery namedQuery: getSavedBuildQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	public ArrayList<NamedCodeCommentQuery> getSavedCodeCommentQueries() {
		if (savedCodeCommentQueries == null) {
			savedCodeCommentQueries = new ArrayList<>();
			savedCodeCommentQueries.add(new NamedCodeCommentQuery("All", "all"));
			savedCodeCommentQueries.add(new NamedCodeCommentQuery("Created by me", "created by me"));
			savedCodeCommentQueries.add(new NamedCodeCommentQuery("Created recently", "\"Create Date\" is after \"last week\""));
			savedCodeCommentQueries.add(new NamedCodeCommentQuery("Updated recently", "\"Update Date\" is after \"last week\""));
		}
		return savedCodeCommentQueries;
	}

	public void setSavedCodeCommentQueries(ArrayList<NamedCodeCommentQuery> savedCodeCommentQueries) {
		this.savedCodeCommentQueries = savedCodeCommentQueries;
	}
	
	public ArrayList<NamedBuildQuery> getSavedBuildQueries() {
		if (savedBuildQueries == null) {
			savedBuildQueries = new ArrayList<>();
			savedBuildQueries.add(new NamedBuildQuery("All", "all"));
			savedBuildQueries.add(new NamedBuildQuery("Successful", "successful"));
			savedBuildQueries.add(new NamedBuildQuery("Failed", "failed"));
			savedBuildQueries.add(new NamedBuildQuery("In error", "in error"));
			savedBuildQueries.add(new NamedBuildQuery("Running", "running"));
			savedBuildQueries.add(new NamedBuildQuery("Build recently", "\"Build Date\" is after \"last week\""));
		}
		return savedBuildQueries;
	}

	public void setSavedBuildQueries(ArrayList<NamedBuildQuery> savedBuildQueries) {
		this.savedBuildQueries = savedBuildQueries;
	}

	public ArrayList<String> getIssueListFields() {
		if (issueListFields == null) {
			issueListFields = new ArrayList<>();
	    	issueListFields.add(IssueConstants.FIELD_NUMBER);
	    	issueListFields.add(IssueConstants.FIELD_STATE);
	    	issueListFields.add(IssueConstants.FIELD_TITLE);
			issueListFields.add("Type");
			issueListFields.add("Priority");
			issueListFields.add(IssueConstants.FIELD_SUBMITTER);
			issueListFields.add("Assignee");
		}
		return issueListFields;
	}
	
	public void setIssueListFields(ArrayList<String> issueListFields) {
		this.issueListFields = issueListFields;
	}

	public Collection<IssueQuerySetting> getIssueQuerySettings() {
		return issueQuerySettings;
	}

	public void setIssueQuerySettings(Collection<IssueQuerySetting> issueQuerySettings) {
		this.issueQuerySettings = issueQuerySettings;
	}

	public Collection<CommitQuerySetting> getCommitQuerySettings() {
		return commitQuerySettings;
	}

	public void setCommitQuerySettings(Collection<CommitQuerySetting> commitQuerySettings) {
		this.commitQuerySettings = commitQuerySettings;
	}

	public Collection<PullRequestQuerySetting> getPullRequestQuerySettings() {
		return pullRequestQuerySettings;
	}

	public void setPullRequestQuerySettings(Collection<PullRequestQuerySetting> pullRequestQuerySettings) {
		this.pullRequestQuerySettings = pullRequestQuerySettings;
	}

	public Collection<CodeCommentQuerySetting> getCodeCommentQuerySettings() {
		return codeCommentQuerySettings;
	}

	public void setCodeCommentQuerySettings(Collection<CodeCommentQuerySetting> codeCommentQuerySettings) {
		this.codeCommentQuerySettings = codeCommentQuerySettings;
	}
	
	public Collection<BuildQuerySetting> getBuildQuerySettings() {
		return buildQuerySettings;
	}

	public void setBuildQuerySettings(Collection<BuildQuerySetting> buildQuerySettings) {
		this.buildQuerySettings = buildQuerySettings;
	}

	public long getVersion() {
		return version;
	}

	public List<BlobIdent> getChildren(BlobIdent blobIdent, BlobIdentFilter blobIdentFilter) {
		return getChildren(blobIdent, blobIdentFilter, getObjectId(blobIdent.revision));
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
			RevCommit commit = getRevCommit(revision);
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

	public ArrayList<IssueBoard> getIssueBoards() {
		if (issueBoards == null) {
			issueBoards = new ArrayList<>();
			IssueBoard board = new IssueBoard();
			board.setName(IssueConstants.FIELD_STATE);
			board.setIdentifyField(IssueConstants.FIELD_STATE);
			board.setColumns(Lists.newArrayList("Open", "Assigned", "Closed"));
			board.setDisplayFields(Lists.newArrayList(IssueConstants.FIELD_STATE, "Type", "Priority", "Assignee", "Resolution", "Duplicate With"));
			issueBoards.add(board);
		}
		return issueBoards;
	}

	public void setIssueBoards(ArrayList<IssueBoard> issueBoards) {
		this.issueBoards = issueBoards;
	}

	public ArrayList<WebHook> getWebHooks() {
		if (webHooks == null)
			webHooks = new ArrayList<>();
		return webHooks;
	}

	public void setWebHooks(ArrayList<WebHook> webHooks) {
		this.webHooks = webHooks;
	}

	@Nullable
	public TagProtection getTagProtection(String tagName, User user) {
		for (TagProtection protection: tagProtections) {
			if (protection.isEnabled() 
					&& PathUtils.matchChildAware(protection.getTag(), tagName)
					&& protection.getSubmitter().matches(this, user)) {
				return protection;
			}
		}
		return null;
	}
	
	@Nullable
	public BranchProtection getBranchProtection(String branchName, @Nullable User user) {
		for (BranchProtection protection: branchProtections) {
			if (protection.isEnabled() 
					&& PathUtils.matchChildAware(protection.getBranch(), branchName)
					&& protection.getSubmitter().matches(this, user)) {
				return protection;
			}
		}
		return null;
	}

	public ProjectFacade getFacade() {
		return new ProjectFacade(this);
	}

	public RevCommit getLastCommit() {
		if (lastCommitOptional == null) {
			RevCommit lastCommit = null;
			try {
				for (Ref ref: getRepository().getRefDatabase().getRefs(Constants.R_HEADS).values()) {
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
			lastCommitOptional = Optional.fromNullable(lastCommit);
		}
		return lastCommitOptional.orNull();
	}

	@Override
	public String toString() {
		return getName();
	}

	public List<User> getAuthors(String filePath, ObjectId commitId, @Nullable Range range) {
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
	
}
