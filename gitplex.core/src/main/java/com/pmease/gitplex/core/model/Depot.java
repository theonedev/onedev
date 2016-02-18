package com.pmease.gitplex.core.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.Submodule;
import com.pmease.commons.git.exception.NotFileException;
import com.pmease.commons.git.exception.ObjectNotExistException;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.LockUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Markdown;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.AndGateKeeper;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.listeners.RefListener;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.UserBelonging;
import com.pmease.gitplex.core.util.validation.DepotName;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"owner", "name"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
@SuppressWarnings("serial")
public class Depot extends AbstractEntity implements UserBelonging {

	private static final String FQN_SEPARATOR = "/";
	
	public static final String REFS_GITPLEX = "refs/gitplex/";
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	private static final int MAX_READ_BLOB_SIZE = 5*1024*1024;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Depot forkedFrom;

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<GateKeeper> gateKeepers = new ArrayList<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<IntegrationPolicy> integrationPolicies = new ArrayList<>();
	
	@Column(nullable=false)
	private Date createdAt = new Date();

	@OneToMany(mappedBy="targetDepot")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<PullRequest> incomingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="sourceDepot")
	private Collection<PullRequest> outgoingRequests = new ArrayList<>();
	
	@OneToMany(mappedBy="depot")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Authorization> authorizations = new ArrayList<>();

    @OneToMany(mappedBy="forkedFrom")
	private Collection<Depot> forks = new ArrayList<>();
    
    private transient Map<BlobIdent, Blob> blobCache;
    
    private transient Map<DiffKey, List<DiffEntry>> diffCache;
    
    private transient Map<String, Commit> commitCache;
    
    private transient Map<String, Optional<ObjectId>> objectIdCache;
    
    private transient Map<String, Map<String, Ref>> prefixRefsCache;
    
    private transient Map<AnyObjectId, Optional<RevObject>> revObjectCache;
    
    private transient Map<RevObject, Optional<RevCommit>> revCommitCache;
    
    private transient Map<String, Optional<Ref>> refCache;
    
    private transient String defaultBranch;
    
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Editable(order=100, description=
			"Specify name of the repository. It will be used to identify the repository when accessing via Git.")
	@DepotName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Specify description of the repository.")
	@Markdown
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

    @Override
	public User getUser() {
		return getOwner();
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

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
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
	
	public Repository openRepository() {
		try {
			return RepositoryCache.open(FileKey.exact(git().depotDir(), FS.DETECTED), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Ref> getBranchRefs() {
		List<Ref> refs = new ArrayList<Ref>(getRefs(Constants.R_HEADS).values());
		Collections.sort(refs, new Comparator<Ref>() {

    		@Override
    		public int compare(Ref o1, Ref o2) {
    			if (o1.getObjectId().equals(o2.getObjectId())) {
    				return o1.getName().compareTo(o2.getName());
    			} else {
    				RevCommit commit1 = getRevCommit(o1.getObjectId());
    				RevCommit commit2 = getRevCommit(o2.getObjectId());
    				return commit2.getCommitTime() - commit1.getCommitTime();
    			}
    		}
    		
    	});
		return refs;
    }
	
	public List<Ref> getTagRefs() {
		List<Ref> refs = new ArrayList<>();
		for (Ref ref: getRefs(Constants.R_TAGS).values()) {
			if (getRevCommit(ref.getObjectId(), false) != null) 
				refs.add(ref);
		}
		Collections.sort(refs, new Comparator<Ref>() {

    		@Override
    		public int compare(Ref o1, Ref o2) {
    			RevObject obj1 = getRevObject(o1.getObjectId());
    			RevObject obj2 = getRevObject(o2.getObjectId());
    			if (obj1 instanceof RevTag && obj2 instanceof RevTag) {
    				RevTag tag1 = (RevTag) obj1;
    				RevTag tag2 = (RevTag) obj2;
    				if (tag1.getTaggerIdent() != null && tag2.getTaggerIdent() != null)
    					return tag2.getTaggerIdent().getWhen().compareTo(tag1.getTaggerIdent().getWhen());
    			}  
    			RevCommit commit1 = getRevCommit(o1.getObjectId());
    			RevCommit commit2 = getRevCommit(o2.getObjectId());
    			if (commit1.getId().equals(commit2.getId()))
    				return o1.getName().compareTo(o2.getName());
    			else
    				return commit2.getCommitTime() - commit1.getCommitTime();
    		}
    		
    	});
		return refs;
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
		return getOwner().getName() + FQN_SEPARATOR + getName();
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
		return new Git(AppLoader.getInstance(StorageManager.class).getDepotDir(this));
	}
	
	/**
	 * Whether or not specified git represents a valid repository git. This can be used to tell 
	 * apart a GitPlex repository git from some other Git repositories.
	 * 
	 * @return
	 * 			<tt>true</tt> if valid; <tt>false</tt> otherwise
	 */
	public boolean isValid() {
		return isUpdateHookValid() && isPostReceiveHookValid();
	}
	
	public boolean isUpdateHookValid() {
        File updateHook = new File(git().depotDir(), "hooks/update");
        if (!updateHook.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(updateHook);
			if (!content.contains("GITPLEX_USER_ID"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!updateHook.canExecute())
        	return false;
        
        return true;
	}
	
	public boolean isPostReceiveHookValid() {
        File postReceiveHook = new File(git().depotDir(), "hooks/post-receive");
        if (!postReceiveHook.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(postReceiveHook);
			if (!content.contains("GITPLEX_USER_ID"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!postReceiveHook.canExecute())
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
		return affinals;
	}
	
	public RevCommit getMergeBase(String ancestor, String descendant) {
		try (	Repository repository = openRepository();
				RevWalk revWalk = new RevWalk(repository)) {
			ObjectId ancestorId = getObjectId(ancestor);
			ObjectId descendantId = getObjectId(descendant);
			revWalk.setRevFilter(RevFilter.MERGE_BASE);
			RevCommit ancestorCommit = getRevCommit(ancestorId);
			RevCommit descendantCommit = getRevCommit(descendantId);
			
			// we should look up commit again as markStart requires that the commit
			// should be resolved in the same revWalk. Also we should not look up
			// against ancestorId directly as it might be id of an annotated tag
			revWalk.markStart(revWalk.lookupCommit(ancestorCommit.getId()));
			revWalk.markStart(revWalk.lookupCommit(descendantCommit.getId()));
			return Preconditions.checkNotNull(revWalk.next());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 			
	}
	
	public boolean isAncestor(String ancestor, String descendant) {
		try (	Repository repository = openRepository();
				RevWalk revWalk = new RevWalk(repository)) {
			ObjectId ancestorId = getObjectId(ancestor);
			ObjectId descendantId = getObjectId(descendant);
			revWalk.setRevFilter(RevFilter.MERGE_BASE);
			RevCommit ancestorCommit = getRevCommit(ancestorId);
			RevCommit descendantCommit = getRevCommit(descendantId);

			// we should look up commit again as markStart requires that the commit
			// should be resolved in the same revWalk. Also we should not look up
			// against ancestorId directly as it might be id of an annotated tag
			revWalk.markStart(revWalk.lookupCommit(ancestorCommit.getId()));
			revWalk.markStart(revWalk.lookupCommit(descendantCommit.getId()));
			return ancestorId.equals(Preconditions.checkNotNull(revWalk.next().getId()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 			
	}

	public String getUrl() {
		return GitPlex.getInstance().guessServerUrl() + "/" + getFQN();
	}
	
	public String getDefaultBranch() {
		if (defaultBranch == null)
			defaultBranch = git().resolveDefaultBranch();
		return defaultBranch;
	}
	
	public String defaultBranchIfNull(@Nullable String revision) {
		if (revision != null)
			return revision;
		else
			return getDefaultBranch();
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
			if (blobIdent.id != null) {
				try (Repository repository = openRepository()) {
					if (blobIdent.isGitLink()) {
						String url = getSubmodules(blobIdent.revision).get(blobIdent.path);
						if (url == null)
							throw new ObjectNotExistException("Unable to find submodule '" + blobIdent.path + "' in .gitmodules");
						blob = new Blob(blobIdent, new Submodule(url, blobIdent.id).toString().getBytes());
					} else if (blobIdent.isTree()) {
						throw new NotFileException("Path '" + blobIdent.path + "' is a tree");
					} else {
						ObjectLoader objectLoader = repository.open(ObjectId.fromString(blobIdent.id), Constants.OBJ_BLOB);
						blob = readBlob(objectLoader, blobIdent);
					}
					getBlobCache().put(blobIdent, blob);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				try (	Repository repository = openRepository(); 
						RevWalk revWalk = new RevWalk(repository)) {
					ObjectId commitId = getObjectId(blobIdent.revision);		
					RevTree revTree = revWalk.parseCommit(commitId).getTree();
					TreeWalk treeWalk = TreeWalk.forPath(repository, blobIdent.path, revTree);
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
		}
		return blob;
	}
	
	public InputStream getInputStream(BlobIdent ident) {
		try (	Repository repository = openRepository(); 
				RevWalk revWalk = new RevWalk(repository)) {
			ObjectId commitId = getObjectId(ident.revision);
			RevTree revTree = revWalk.parseCommit(commitId).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(repository, ident.path, revTree);
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
			ObjectId objectId;
			try (Repository repository = openRepository()) {
				objectId = repository.resolve(revision);
			} catch (RevisionSyntaxException | IOException e) {
				if (!mustExist) {
					objectId = null;
				} else {
					throw new RuntimeException(e);
				}
			}
			optional = Optional.fromNullable(objectId);
			objectIdCache.put(revision, optional);
		}
		if (mustExist && !optional.isPresent())
			throw new ObjectNotExistException("Unable to find revision '" + revision + "'");
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
	
	public List<DiffEntry> getDiffs(String oldRev, String newRev, boolean detectRenames, String...paths) {
		if (diffCache == null)
			diffCache = new HashMap<>();
		
		DiffKey key = new DiffKey(oldRev, newRev, detectRenames, paths);
		List<DiffEntry> diffs = diffCache.get(key);
		if (diffs == null) {
			try (	Repository repository = openRepository();
					DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);) {
		    	diffFormatter.setRepository(repository);
		    	diffFormatter.setDetectRenames(detectRenames);
				AnyObjectId oldCommitId = getObjectId(oldRev);
				AnyObjectId newCommitId = getObjectId(newRev);
				if (paths.length >= 2) {
					List<TreeFilter> pathFilters = new ArrayList<>();
					for (String path: paths)
						pathFilters.add(PathFilter.create(path));
					diffFormatter.setPathFilter(OrTreeFilter.create(pathFilters));
				} else if (paths.length == 1) {
					diffFormatter.setPathFilter(PathFilter.create(paths[0]));
				}
				diffs = new ArrayList<>();
		    	for (DiffEntry entry: diffFormatter.scan(oldCommitId, newCommitId)) {
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
			diffCache.put(key, diffs);
		}
		return diffs;
	}
	
	public Commit getCommit(String commitHash) {
		if (commitCache == null)
			commitCache = new HashMap<>();
		
		Commit commit = commitCache.get(commitHash);
		if (commit == null) {
			commit = git().showRevision(commitHash);
			commitCache.put(commitHash, commit);
		}
		return commit;
	}
	
	public void cacheCommits(List<Commit> commits) {
		if (commitCache == null)
			commitCache = new HashMap<>();
		
		for (Commit commit: commits)
			commitCache.put(commit.getHash(), commit);
	}

	public LastCommitsOfChildren getLastCommitsOfChildren(String revision, @Nullable String path) {
		if (path == null)
			path = "";
		
		final File cacheDir = new File(
				GitPlex.getInstance(StorageManager.class).getCacheDir(this), 
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
		
		try (Repository repository = openRepository()) {
			long time = System.currentTimeMillis();
			LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(repository, commitId, path, cache);
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
	}

	@Nullable
	public Ref getRef(String revision) {
		if (refCache == null)
			refCache = new HashMap<>();
		Optional<Ref> optional = refCache.get(revision);
		if (optional == null) {
			try (Repository repository = openRepository()) {
				optional = Optional.fromNullable(repository.getRef(revision));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			refCache.put(revision, optional);
		}
		return optional.orNull();
	}
	
	public RevObject getRevObject(AnyObjectId revId) {
		return getRevObject(revId, true);
	}
	
	@Nullable
	public RevObject getRevObject(AnyObjectId revId, boolean mustExist) {
		if (revId == null && !mustExist)
			return null;
		
		if (revObjectCache == null)
			revObjectCache = new HashMap<>();
		Optional<RevObject> optional = revObjectCache.get(revId);
		if (optional == null) {
			try (	Repository repository = openRepository();
					RevWalk revWalk = new RevWalk(repository);) {
				optional = Optional.of(revWalk.parseAny(revId));
			} catch (MissingObjectException e) {
				if (!mustExist)
					optional = Optional.absent();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			revObjectCache.put(revId, optional);
		}
		if (mustExist)
			return optional.get();
		else
			return optional.orNull();
	}
	
	public RevCommit getRevCommit(RevObject revObject) {
		return getRevCommit(revObject, true);
	}
	
	@Nullable
	public RevCommit getRevCommit(RevObject revObject, boolean mustExist) {
		if (revObject == null && !mustExist)
			return null;
		
		if (revCommitCache == null)
			revCommitCache = new HashMap<>();
		Optional<RevCommit> optional = revCommitCache.get(revObject);
		if (optional == null) {
			try (	Repository repository = openRepository();
					RevWalk revWalk = new RevWalk(repository);) {
				RevObject peeled = revWalk.peel(revObject);
				if (peeled instanceof RevCommit)
					optional = Optional.of((RevCommit) peeled);
				else
					optional = Optional.absent();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			revCommitCache.put(revObject, optional);
		}
		if (mustExist)
			return optional.get();
		else
			return optional.orNull();
	}
	
	@Nullable
	public RevCommit getRevCommit(AnyObjectId revId, boolean mustExist) {
		return getRevCommit(getRevObject(revId, mustExist), mustExist);
	}

	@Nullable
	public RevCommit getRevCommit(String revision, boolean mustExist) {
		return getRevCommit(getObjectId(revision, mustExist), mustExist);
	}
	
	public RevCommit getRevCommit(String revision) {
		return getRevCommit(revision, true);
	}
	
	public RevCommit getRevCommit(AnyObjectId revId) {
		return getRevCommit(getRevObject(revId), true);
	}
	
	public Map<String, Ref> getRefs(String prefix) {
		if (prefixRefsCache == null)
			prefixRefsCache = new HashMap<>();
		
		Map<String, Ref> cached = prefixRefsCache.get(prefix);
		if (cached == null) {
			try (Repository repository = openRepository()) {
				cached = repository.getRefDatabase().getRefs(prefix); 
				prefixRefsCache.put(prefix, cached);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return cached;
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

    public String getRevisionFQN(String revision) {
    	return getFQN() + ":" + revision;
    }

    public void deleteBranch(String branch) {
    	String refName = GitUtils.branch2ref(branch);
		git().deleteRef(refName);
		for (RefListener listener: GitPlex.getExtensions(RefListener.class))
			listener.onRefUpdate(this, refName, null);
    }
    
    public void createBranch(String branchName, String branchRevision) {
		try (Repository repository = openRepository()) {
			CreateBranchCommand command = org.eclipse.jgit.api.Git.wrap(repository).branchCreate();
			command.setName(branchName);
			command.setStartPoint(getRevCommit(branchRevision));
			command.call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void tag(String tagName, String tagRevision, PersonIdent taggerIdent, @Nullable String tagMessage) {
		try (Repository repository = openRepository();) {
			TagCommand tag = org.eclipse.jgit.api.Git.wrap(repository).tag();
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
		git().deleteRef(refName);
		for (RefListener listener: GitPlex.getExtensions(RefListener.class))
			listener.onRefUpdate(this, refName, null);
    }
    
	private static class DiffKey implements Serializable {
		String oldRev;
		
		String newRev;
		
		String[] paths;
		
		boolean detectRenames;
		
		DiffKey(String oldRev, String newRev, boolean detectRenames, String...paths) {
			this.oldRev = oldRev;
			this.newRev = newRev;
			this.detectRenames = detectRenames;
			this.paths = paths;
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof DiffKey))
				return false;
			if (this == other)
				return true;
			DiffKey otherKey = (DiffKey) other;
			return Objects.equal(oldRev, otherKey.oldRev) 
					&& Objects.equal(newRev, otherKey.newRev) 
					&& Objects.equal(paths, otherKey.paths)
					&& Objects.equal(detectRenames, otherKey.detectRenames);
		}

		public int hashCode() {
			return Objects.hashCode(oldRev, newRev, paths, detectRenames);
		}
		
	}

}
