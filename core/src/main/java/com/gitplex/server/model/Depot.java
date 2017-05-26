package com.gitplex.server.model;

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
import javax.persistence.UniqueConstraint;
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

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.launcher.loader.LoaderUtils;
import com.gitplex.server.GitPlex;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.BlobIdentFilter;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.RefInfo;
import com.gitplex.server.git.Submodule;
import com.gitplex.server.git.exception.NotFileException;
import com.gitplex.server.git.exception.ObjectNotFoundException;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.VisitInfoManager;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.CommitMessageTransformSetting;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.security.protectedobject.AccountBelonging;
import com.gitplex.server.security.protectedobject.ProtectedObject;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.LockUtils;
import com.gitplex.server.util.PathUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.Markdown;
import com.gitplex.server.util.validation.annotation.DepotName;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

@Entity
@Table(
		indexes={@Index(columnList="g_forkedFrom_id"), @Index(columnList="g_account_id"), @Index(columnList="name")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"g_account_id", "name"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
@Editable
public class Depot extends AbstractEntity implements AccountBelonging {

	private static final long serialVersionUID = 1L;

	public static final String FQN_SEPARATOR = "/";
	
	public static final String REF_FQN_SEPARATOR = ":";
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account account;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Depot forkedFrom;

	@Column(nullable=false)
	private String name;
	
	@Lob
	@Column(length=65535)
	private String description;
	
	private boolean publicRead;

	@Lob
	@Column(length=65535, name="COMMIT_MSG_TRANSFORM")
	private CommitMessageTransformSetting commitMessageTransformSetting;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * branch and tag protection settings upon depot renaming/deletion
	 */
	@Version
	private long version;
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<BranchProtection> branchProtections = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<TagProtection> tagProtections = new ArrayList<>();
	
	@Column(nullable=false)
	private Date createdAt = new Date();

	@OneToMany(mappedBy="targetDepot", cascade=CascadeType.REMOVE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceDepot")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
    @OneToMany(mappedBy="forkedFrom")
	private Collection<Depot> forks = new ArrayList<>();
    
	@OneToMany(mappedBy="depot", cascade=CascadeType.REMOVE)
	private Collection<TeamAuthorization> authorizedTeams = new ArrayList<>();
	
	@OneToMany(mappedBy="depot", cascade=CascadeType.REMOVE)
	private Collection<UserAuthorization> authorizedUsers = new ArrayList<>();
	
	private transient Repository repository;
	
    private transient Map<BlobIdent, Blob> blobCache;
    
    private transient Map<String, Optional<ObjectId>> objectIdCache;
    
    private transient Map<ObjectId, Optional<RevCommit>> commitCache;
    
    private transient Map<String, Optional<Ref>> refCache;
    
    private transient Optional<String> defaultBranch;
    
    @Override
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Editable(order=100)
	@DepotName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Optionally describe the repository")
	@Markdown
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=300, name="Public", description="Whether or not this repository can be read by everyone")
    public boolean isPublicRead() {
		return publicRead;
	}

	public void setPublicRead(boolean publicRead) {
		this.publicRead = publicRead;
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

	public Depot getForkedFrom() {
		return forkedFrom;
	}

	public void setForkedFrom(Depot forkedFrom) {
		this.forkedFrom = forkedFrom;
	}

	public Collection<Depot> getForks() {
		return forks;
	}

	public void setForks(Collection<Depot> forks) {
		this.forks = forks;
	}
	
	public List<RefInfo> getBranches() {
		return getRefInfos(Constants.R_HEADS);
    }
	
	public List<RefInfo> getTags() {
		return getRefInfos(Constants.R_TAGS);
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

    @Override
	public boolean has(ProtectedObject object) {
		if (object instanceof Depot) {
			Depot depot = (Depot) object;
			return depot.getId().equals(getId());
		} else {
			return false;
		}
	}

	public String getFQN() {
		return getAccount().getName() + FQN_SEPARATOR + getName();
	}
	
	public static String getNameByFQN(String repositoryFQN) {
		return StringUtils.substringAfterLast(repositoryFQN, FQN_SEPARATOR);
	}
	
	public static String getUserNameByFQN(String repositoryFQN) {
		return StringUtils.substringBeforeLast(repositoryFQN, FQN_SEPARATOR);
	}
	
	@Override
	public String toString() {
		return getFQN();
	}
	
	public Git git() {
		return Git.wrap(getRepository()); 
	}
	
	public File getGitDir() {
		return AppLoader.getInstance(StorageManager.class).getGitDir(this);
	}
	
	/**
	 * Find fork root of this repository. 
	 * 
	 * @return
	 * 			fork root of this repository
	 */
	public Depot getForkRoot() {
		if (forkedFrom != null) 
			return forkedFrom.getForkRoot();
		else 
			return this;
	}
	
	/**
	 * Get all descendant repositories forking from current repository.
	 * 
	 * @return
	 * 			all descendant repositories forking from current repository
	 */
	public List<Depot> getForkDescendants() {
		List<Depot> descendants = new ArrayList<>();
		if (getDefaultBranch() != null)
			descendants.add(this);
		for (Depot fork: getForks()) { 
			descendants.addAll(fork.getForkDescendants());
		}
		
		return descendants;
	}
	
	public Repository getRepository() {
		if (repository == null) {
			repository = GitPlex.getInstance(DepotManager.class).getRepository(this);
		}
		return repository;
	}
	
	public String getUrl() {
		return GitPlex.getInstance(ConfigManager.class).getSystemSetting().getServerUrl() + "/" + getFQN();
	}
	
	@Nullable
	public String getDefaultBranch() {
		if (defaultBranch == null) {
			try {
				Ref headRef = getRepository().findRef("HEAD");
				if (headRef != null 
						&& headRef.isSymbolic() 
						&& headRef.getTarget().getName().startsWith(Constants.R_HEADS) 
						&& headRef.getObjectId() != null) {
					defaultBranch = Optional.of(Repository.shortenRefName(headRef.getTarget().getName()));
				} else {
					defaultBranch = Optional.absent();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return defaultBranch.orNull();
	}
	
	public void setDefaultBranch(String defaultBranchName) {
		RefUpdate refUpdate = updateRef("HEAD");
		GitUtils.linkRef(refUpdate, GitUtils.branch2ref(defaultBranchName));
		defaultBranch = null;
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
				GitPlex.getInstance(StorageManager.class).getInfoDir(this), 
				"last_commits/" + path + "/gitplex_last_commits");
		
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
		
		for (String line: LoaderUtils.splitAndTrim(content, "\r\n")) {
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
			Throwables.propagate(e);
		}
    	
    	Subject subject = SecurityUtils.getSubject();
    	GitPlex.getInstance(UnitOfWork.class).doAsync(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					Depot depot = GitPlex.getInstance(DepotManager.class).load(getId());
					GitPlex.getInstance(ListenerRegistry.class).post(
							new RefUpdated(depot, refName, commitId, ObjectId.zeroId()));
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
			command.setStartPoint(getRevCommit(branchRevision));
			command.call();
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
    	GitPlex.getInstance(UnitOfWork.class).doAsync(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					Depot depot = GitPlex.getInstance(DepotManager.class).load(getId());
					GitPlex.getInstance(ListenerRegistry.class).post(
							new RefUpdated(depot, refName, commitId, ObjectId.zeroId()));
				} finally {
					ThreadContext.unbindSubject();
				}
			}
    		
    	});
    }
    
	public Collection<TeamAuthorization> getAuthorizedTeams() {
		return authorizedTeams;
	}

	public void setAuthorizedTeams(Collection<TeamAuthorization> authorizedTeams) {
		this.authorizedTeams = authorizedTeams;
	}

	public Collection<UserAuthorization> getAuthorizedUsers() {
		return authorizedUsers;
	}

	public void setAuthorizedUsers(Collection<UserAuthorization> authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}

	public long getVersion() {
		return version;
	}

	public boolean matches(@Nullable String searchTerm) {
		if (searchTerm == null)
			searchTerm = "";
		else
			searchTerm = searchTerm.toLowerCase().trim();
		
		return getName().toLowerCase().contains(searchTerm);
	}
	
	public boolean matchesFQN(@Nullable String searchTerm) {
		if (searchTerm == null)
			searchTerm = "";
		else
			searchTerm = searchTerm.toLowerCase().trim();
		
		return getFQN().toLowerCase().contains(searchTerm);
	}
	
	public RefUpdate updateRef(String refName) {
		try {
			return getRepository().updateRef(refName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int compareTo(AbstractEntity entity) {
		Depot depot = (Depot) entity;
		if (getAccount().equals(depot.getAccount())) {
			return getName().compareTo(depot.getName());
		} else {
			return getAccount().compareTo(depot.getAccount());
		}
	}
	
	public List<BlobIdent> getChildren(BlobIdent blobIdent, BlobIdentFilter blobIdentFilter) {
		Repository repository = getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevTree revTree = revWalk.parseCommit(getObjectId(blobIdent.revision)).getTree();
			
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

	public static int compareLastVisit(Depot depot1, Depot depot2) {
		Account user = SecurityUtils.getAccount();
		if (user != null) {
			Date date1 = GitPlex.getInstance(VisitInfoManager.class).getVisitDate(user, depot1);
			Date date2 = GitPlex.getInstance(VisitInfoManager.class).getVisitDate(user, depot2);
			if (date1 != null) {
				if (date2 != null)
					return date2.compareTo(date1);
				else
					return -1;
			} else {
				if (date2 != null)
					return 1;
				else
					return depot1.compareTo(depot2);
			}
		} else {
			return depot1.compareTo(depot2);
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

	@Nullable
	public TagProtection getTagProtection(String tagName) {
		for (TagProtection protection: tagProtections) {
			if (PathUtils.matchChildAware(protection.getTag(), tagName))
				return protection;
		}
		return null;
	}
	
	@Nullable
	public BranchProtection getBranchProtection(String branchName) {
		for (BranchProtection protection: branchProtections) {
			if (PathUtils.matchChildAware(protection.getBranch(), branchName))
				return protection;
		}
		return null;
	}
	
}
