package com.pmease.gitplex.core.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Multiline;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.Pair;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.AndGateKeeper;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.UserBelonging;
import com.pmease.gitplex.core.validation.RepositoryName;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"}), 
		@UniqueConstraint(columnNames={"owner", "forkedFrom"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
@SuppressWarnings("serial")
public class Repository extends AbstractEntity implements UserBelonging {

	public static final String REFS_GITPLEX = "refs/gitplex/";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;

	private boolean forkable = true;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private Repository forkedFrom;

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	@Lob
	@Column(nullable=false)
	private ArrayList<GateKeeper> gateKeepers = new ArrayList<>();
	
	@Lob
	@Column(nullable=false)
	private ArrayList<IntegrationPolicy> integrationPolicies = new ArrayList<>();
	
	@Column(nullable=false)
	private Date createdAt = new Date();

	@OneToMany(mappedBy="repository", cascade=CascadeType.REMOVE)
	private Collection<Authorization> authorizations = new ArrayList<>();

	@OneToMany(mappedBy="repository", cascade=CascadeType.REMOVE)
    private Collection<Branch> branches = new ArrayList<>();

    @OneToMany(mappedBy="forkedFrom")
	private Collection<Repository> forks = new ArrayList<>();
    
    private transient Map<BlobInfo, byte[]> blobContentCache = new HashMap<>();
    
    private transient Map<BlobInfo, Optional<BlobText>> blobTextCache = new HashMap<>(); 
    
    private transient Map<CommitRange, List<Change>> changesCache = new HashMap<>();
    
    private transient Map<String, Commit> commitCache = new HashMap<>();
    
    private transient Map<String, Optional<String>> refValueCache = new HashMap<>();
    
    private transient Branch defaultBranch;
    
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Editable(order=100, description=
			"Specify name of the repository. It will be used to identify the repository when accessing via Git.")
	@RepositoryName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Specify description of the repository.")
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    @Editable(order=450, description="Whether or not this repository can be forked.")
    public boolean isForkable() {
        return forkable;
    }

    public void setForkable(boolean forkable) {
        this.forkable = forkable;
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

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

	public Repository getForkedFrom() {
		return forkedFrom;
	}

	public void setForkedFrom(Repository forkedFrom) {
		this.forkedFrom = forkedFrom;
	}

	public Collection<Repository> getForks() {
		return forks;
	}

	public void setForks(Collection<Repository> forks) {
		this.forks = forks;
	}

	/**
	 * Get branches for this repository from database. The result might be 
	 * different from actual branches in repository. To get actual 
	 * branches in repository, call {@link BranchManager#listBranches(Repository)} 
	 * instead.
	 * 
	 * @return
	 *         collection of branches available in database for this repository 
	 */
	public Collection<Branch> getBranches() {
        return branches;
    }

    public void setBranches(Collection<Branch> branches) {
        this.branches = branches;
    }

    @Override
	public boolean has(ProtectedObject object) {
		if (object instanceof Repository) {
			Repository repository = (Repository) object;
			return repository.getId().equals(getId());
		} else {
			return false;
		}
	}

	public String getFullName() {
		return getOwner().getName() + "/" + getName();
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
	
	public Git git() {
		return new Git(AppLoader.getInstance(StorageManager.class).getRepoDir(this));
	}
	
	/**
	 * Whether or not specified git represents a valid repository git. This can be used to tell 
	 * apart a GitPlex repository git from some other Git repositories.
	 * 
	 * @return
	 * 			<tt>true</tt> if valid; <tt>false</tt> otherwise
	 */
	public static boolean isValid(Git git) {
        File updateHook = new File(git.repoDir(), "hooks/update");
        if (!updateHook.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(updateHook);
			return content.contains("GITOP_USER_ID");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
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
	public @Nullable Repository findForkRoot() {
		if (forkedFrom != null) {
			Repository forkedRoot = forkedFrom.findForkRoot();
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
	public List<Repository> findForkDescendants() {
		List<Repository> descendants = new ArrayList<>();
		for (Repository fork: getForks()) { 
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
	public List<Repository> findAffinals() {
		List<Repository> affinals = new ArrayList<Repository>();
		Repository forkRoot = findForkRoot();
		if (forkRoot != null) {
			affinals.add(forkRoot);
			affinals.addAll(forkRoot.findForkDescendants());
		} else {
			affinals.add(this);
			affinals.addAll(findForkDescendants());
		}
		return affinals;
	}

	public String getUrl() {
		return GitPlex.getInstance().getServerUrl() + "/" + getFullName();
	}
	
	public Branch getDefaultBranch() {
		if (defaultBranch == null) {
			for (Branch branch: getBranches()) {
				if (branch.isDefault()) {
					defaultBranch = branch;
					break;
				}
			}
			Preconditions.checkNotNull(defaultBranch);
		}
		return defaultBranch;
	}
	
	public String defaultBranchIfNull(@Nullable String revision) {
		if (revision != null)
			return revision;
		else
			return getDefaultBranch().getName();
	}
	
	/**
	 * Read blob content and cache result in repository in case the same blob 
	 * content is requested again.
	 * 
	 * @param blobInfo
	 * 			info of the blob
	 * @return
	 * 			content of the blob
	 */
	public byte[] getBlobContent(BlobInfo blobInfo) {
		byte[] blobContent = blobContentCache.get(blobInfo);
		if (blobContent == null) {
			blobContent = git().readBlob(blobInfo);
			blobContentCache.put(blobInfo, blobContent);
		}
		return blobContent;
	}
	
	/**
	 * Read blob content as text and cache result in repository in case the same 
	 * blob text is requested again.
	 * 
	 * @param commit
	 * 			commit of the blob
	 * @param blobPath
	 * 			path of the blob
	 * @param blobMode
	 * 			mode of the blob
	 * @return
	 * 			text of the blob, or <tt>null</tt> if the blob content can not be 
	 * 			converted to text
	 */
	@Nullable
	public BlobText getBlobText(BlobInfo blobInfo) {
		Optional<BlobText> optional = blobTextCache.get(blobInfo);
		if (optional == null) {
			byte[] blobContent = getBlobContent(blobInfo);
			optional = Optional.fromNullable(BlobText.from(blobContent, blobInfo.getPath(), blobInfo.getMode()));
			blobTextCache.put(blobInfo, optional);
		}
		return optional.orNull();
	}
	
	@Nullable
	public String getRefValue(String refName) {
		Optional<String> optional = refValueCache.get(refName);
		if (optional == null) {
			optional = Optional.fromNullable(git().parseRevision(refName, false));
			refValueCache.put(refName, optional);
		}
		return optional.orNull();
	}

	public List<Change> getChanges(String fromCommit, String toCommit) {
		CommitRange range = new CommitRange(fromCommit, toCommit);
		List<Change> changes = changesCache.get(range);
		if (changes == null) {
			changes = git().listFileChanges(fromCommit, toCommit, null, true);
			changesCache.put(range, changes);
		}
		return changes;
	}
	
	public Commit getCommit(String commitHash) {
		Commit commit = commitCache.get(commitHash);
		if (commit == null) {
			commit = git().showRevision(commitHash);
			commitCache.put(commitHash, commit);
		}
		return commit;
	}
	
	public void cacheCommits(List<Commit> commits) {
		for (Commit commit: commits)
			commitCache.put(commit.getHash(), commit);
	}

	private static class CommitRange extends Pair<String, String> {
		CommitRange(String fromCommit, String toCommit) {
			super(fromCommit, toCommit);
		}
	}
	
}
