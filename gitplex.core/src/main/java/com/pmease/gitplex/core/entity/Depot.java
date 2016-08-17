package com.pmease.gitplex.core.entity;

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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.MyersDiff;
import org.eclipse.jgit.diff.RawTextComparator;
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
import org.eclipse.jgit.util.io.NullOutputStream;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RefInfo;
import com.pmease.commons.git.Submodule;
import com.pmease.commons.git.exception.NotFileException;
import com.pmease.commons.git.exception.ObjectNotExistException;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.LockUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Markdown;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.support.IntegrationPolicy;
import com.pmease.gitplex.core.event.RefUpdated;
import com.pmease.gitplex.core.gatekeeper.AndGateKeeper;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.security.protectedobject.AccountBelonging;
import com.pmease.gitplex.core.security.protectedobject.ProtectedObject;
import com.pmease.gitplex.core.util.validation.DepotName;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"g_account_id", "name"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
@Editable
public class Depot extends AbstractEntity implements AccountBelonging {

	private static final long serialVersionUID = 1L;

	public static final String FQN_SEPARATOR = "/";
	
	public static final String REF_FQN_SEPARATOR = ":";
	
	public static final String REFS_GITPLEX = "refs/gitplex/";
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	private static final int MAX_READ_BLOB_SIZE = 5*1024*1024;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account account;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Depot forkedFrom;

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private boolean publicRead;

	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * gate keepers and integration policies upon depot renaming/deletion
	 */
	@Version
	private long version;
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<GateKeeper> gateKeepers = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<IntegrationPolicy> integrationPolicies = new ArrayList<>();
	
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

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	@NotNull
	@Valid
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	public void setGateKeepers(ArrayList<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Valid
	public List<IntegrationPolicy> getIntegrationPolicies() {
		return integrationPolicies;
	}

	public void setIntegrationPolicies(ArrayList<IntegrationPolicy> integrationPolicies) {
		this.integrationPolicies = integrationPolicies;
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
	
	public File getDirectory() {
		return AppLoader.getInstance(StorageManager.class).getGitDir(this);
	}
	
	/**
	 * Whether or not specified git represents a valid repository git. This can be used to tell 
	 * apart a GitPlex repository git from some other Git repositories.
	 * 
	 * @return
	 * 			<tt>true</tt> if valid; <tt>false</tt> otherwise
	 */
	public boolean isValid() {
		return isGitHookValid("pre-receive") && isGitHookValid("post-receive");
	}
	
	public boolean isGitHookValid(String hookName) {
        File hookFile = new File(getDirectory(), "hooks/" + hookName);
        if (!hookFile.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(hookFile);
			if (!content.contains("GITPLEX_USER_ID"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!hookFile.canExecute())
        	return false;
        
        return true;
	}
	
	public GateKeeper getGateKeeper() {
		AndGateKeeper andGateKeeper = new AndGateKeeper();
		for (GateKeeper each: getGateKeepers())
			andGateKeeper.getGateKeepers().add(each);
		return andGateKeeper;
	}

	/**
	 * Find fork root of this repository. 
	 * 
	 * @return
	 * 			fork root of this repository, or <tt>null</tt> if the repository is not 
	 * 			forked from any other repository  
	 */
	public @Nullable Depot findForkRoot() {
		if (forkedFrom != null) {
			Depot forkedRoot = forkedFrom.findForkRoot();
			if (forkedRoot != null)
				return forkedRoot;
			else
				return forkedFrom;
		} else {
			return null;
		}
	}
	
	/**
	 * Find all descendant repositories forking from current repository.
	 * 
	 * @return
	 * 			all descendant repositories forking from current repository
	 */
	public List<Depot> findForkDescendants() {
		List<Depot> descendants = new ArrayList<>();
		for (Depot fork: getForks()) { 
			if (fork.getDefaultBranch() != null)
				descendants.add(fork);
			descendants.addAll(fork.findForkDescendants());
		}
		
		return descendants;
	}
	
	/**
	 * Find all comparable repositories of current repository. Comparable repositories can 
	 * be connected via forks, and can be compared/pulled. 
	 * 
	 * @return
	 * 			comparable repositories of current repository, with current repository also 
	 * 			included in the collection
	 */
	public List<Depot> findAffinals() {
		List<Depot> affinals = new ArrayList<Depot>();
		Depot forkRoot = findForkRoot();
		if (forkRoot != null) {
			affinals.add(forkRoot);
			affinals.addAll(forkRoot.findForkDescendants());
		} else {
			affinals.add(this);
			affinals.addAll(findForkDescendants());
		}
		affinals.sort((repo1, repo2) -> {
			if (repo1.getAccount().equals(repo2.getAccount()))
				return repo1.getName().compareTo(repo2.getName());
			else
				return repo1.getAccount().getName().compareTo(repo2.getAccount().getName());
		});
		return affinals;
	}
	
	public Repository getRepository() {
		if (repository == null) {
			repository = GitPlex.getInstance(DepotManager.class).getRepository(this);
		}
		return repository;
	}
	
	public RevCommit getMergeBase(String commit1, String commit2) {
		return GitUtils.getMergeBase(getRepository(), getObjectId(commit1), getObjectId(commit2));
	}
	
	public boolean isMergedInto(String base, String tip) {
		return GitUtils.isMergedInto(getRepository(), getObjectId(base), getObjectId(tip));
	}

	public String getUrl() {
		return GitPlex.getInstance(ConfigManager.class).getSystemSetting().getServerUrl() + "/" + getFQN();
	}
	
	@Nullable
	public String getDefaultBranch() {
		if (defaultBranch == null) {
			try {
				Ref headRef = getRepository().findRef("HEAD");
				if (headRef.isSymbolic() 
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
	
	private Blob readBlob(ObjectLoader objectLoader, BlobIdent ident) {
		long blobSize = objectLoader.getSize();
		if (blobSize > MAX_READ_BLOB_SIZE) {
			try (InputStream is = objectLoader.openStream()) {
				byte[] bytes = new byte[MAX_READ_BLOB_SIZE];
				is.read(bytes);
				return new Blob(ident, bytes, blobSize);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new Blob(ident, objectLoader.getCachedBytes());
		}
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
	 * 			ObjectNotExistException if blob of specified ident can not be found in repository 
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
					if (blobIdent.isGitLink()) {
						String url = getSubmodules(blobIdent.revision).get(blobIdent.path);
						if (url == null)
							throw new ObjectNotExistException("Unable to find submodule '" + blobIdent.path + "' in .gitmodules");
						String hash = treeWalk.getObjectId(0).name();
						blob = new Blob(blobIdent, new Submodule(url, hash).toString().getBytes());
					} else if (blobIdent.isTree()) {
						throw new NotFileException("Path '" + blobIdent.path + "' is a tree");
					} else {
						ObjectLoader objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
						blob = readBlob(objectLoader, blobIdent);
					}
					getBlobCache().put(blobIdent, blob);
				} else {
					throw new ObjectNotExistException("Unable to find blob path '" + blobIdent.path + "' in revision '" + blobIdent.revision + "'");
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
				throw new ObjectNotExistException("Unable to find blob path '" + ident.path + "' in revision '" + ident.revision + "'");
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
			throw new ObjectNotExistException("Unable to find object '" + revision + "'");
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
	
	public List<DiffEntry> getDiffs(String oldRev, String newRev) {
		List<DiffEntry> diffs = new ArrayList<>();
		try (DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);) {
	    	diffFormatter.setRepository(getRepository());
	    	diffFormatter.setDetectRenames(true);
	    	diffFormatter.setDiffAlgorithm(MyersDiff.INSTANCE);
	    	diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
			AnyObjectId oldRevId = getObjectId(oldRev);
			AnyObjectId newRevId = getObjectId(newRev);
	    	for (DiffEntry entry: diffFormatter.scan(oldRevId, newRevId)) {
	    		if (!Objects.equal(entry.getOldPath(), entry.getNewPath())
	    				|| !Objects.equal(entry.getOldMode(), entry.getNewMode())
	    				|| entry.getOldId()==null || !entry.getOldId().isComplete()
	    				|| entry.getNewId()== null || !entry.getNewId().isComplete()
	    				|| !entry.getOldId().equals(entry.getNewId())) {
	    			diffs.add(entry);
	    		}
	    	}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}			
		return diffs;
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
	public RevCommit getRevCommit(String revision, boolean mustExist) {
		RevCommit commit;
		ObjectId revId = getObjectId(revision, mustExist);
		if (revId != null) {
			try (RevWalk revWalk = new RevWalk(getRepository())) {
				commit = GitUtils.parseCommit(revWalk, revId);
			}
		} else {
			commit = null;
		}
		if (mustExist && commit == null)
			throw new ObjectNotExistException("Unable to find commit: " + revision);
		else
			return commit;
	}
	
	public RevCommit getRevCommit(String revision) {
		return getRevCommit(revision, true);
	}
	
	@Nullable
	public RevCommit getRevCommit(ObjectId revId, boolean mustExist) {
		try (RevWalk revWalk = new RevWalk(getRepository())) {
			RevCommit commit = GitUtils.parseCommit(revWalk, revId);
			if (mustExist && commit == null)
				throw new ObjectNotExistException("Unable to find commit: " + revId.name());
			else
				return commit;
		}
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
    	ObjectId commitId = getRevCommit(refName).getId();
    	try {
			git().branchDelete().setForce(true).setBranchNames(branch).call();
		} catch (Exception e) {
			Throwables.propagate(e);
		}
    	GitPlex.getInstance(UnitOfWork.class).doAsync(new Runnable() {

			@Override
			public void run() {
				Depot depot = GitPlex.getInstance(DepotManager.class).load(getId());
				GitPlex.getInstance(ListenerRegistry.class).post(
						new RefUpdated(depot, refName, commitId, ObjectId.zeroId()));
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
    	GitPlex.getInstance(UnitOfWork.class).doAsync(new Runnable() {

			@Override
			public void run() {
				Depot depot = GitPlex.getInstance(DepotManager.class).load(getId());
				GitPlex.getInstance(ListenerRegistry.class).post(
						new RefUpdated(depot, refName, commitId, ObjectId.zeroId()));
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

}
